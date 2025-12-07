import os
from datetime import datetime
from flask import Blueprint, jsonify, request, current_app
from flask_jwt_extended import jwt_required, get_jwt_identity
from werkzeug.security import check_password_hash, generate_password_hash
from werkzeug.utils import secure_filename
from app.models import User, Follow
from app.db import db

user_bp = Blueprint("user_bp", __name__)


def _user_payload(user: User, current_user_id: int | None = None):
    followers_count = Follow.query.filter_by(following_id=user.id).count()
    following_count = Follow.query.filter_by(follower_id=user.id).count()
    is_following = None

    if current_user_id:
        is_following = Follow.query.filter_by(
            follower_id=current_user_id,
            following_id=user.id
        ).first() is not None

    return {
        "id": user.id,
        "nom_utilisateur": user.nom_utilisateur,
        "created_at": user.created_at.isoformat(),
        "followers_count": followers_count,
        "following_count": following_count,
        "photo_url": user.photo_url,
        "is_following": is_following,
    }


@user_bp.get("/")
@jwt_required()
def list_users():
    current_user_id = get_jwt_identity()
    utilisateurs = User.query.all()
    result = [_user_payload(u, current_user_id) for u in utilisateurs]
    return jsonify(result), 200


@user_bp.get("/<int:user_id>")
@jwt_required()
def get_user(user_id):
    user = User.query.get(user_id)
    if not user:
        return jsonify({"error": "User pas trouver"}), 404

    current_user_id = get_jwt_identity()
    return jsonify(_user_payload(user, current_user_id)), 200

@user_bp.get("/current_user")
@jwt_required()
def get_current_user():
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    if not user:
        return jsonify({"error": "User pas trouvé"}), 404

    return jsonify(_user_payload(user, current_user_id)), 200


@user_bp.get("/profil")
@jwt_required()
def get_profile():
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    if not user:
        return jsonify({"error": "User pas trouvé"}), 404

    return jsonify(_user_payload(user, current_user_id)), 200


@user_bp.put("/profil")
@jwt_required()
def update_profile():
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    if not user:
        return jsonify({"error": "User pas trouvé"}), 404

    data = request.get_json() or {}
    new_name = data.get("nom_utilisateur")

    if new_name and new_name != user.nom_utilisateur:
        existing = User.query.filter(
            User.nom_utilisateur == new_name,
            User.id != user.id
        ).first()
        if existing:
            return jsonify({"error": "Nom d'utilisateur déjà pris"}), 400
        user.nom_utilisateur = new_name

    db.session.commit()

    return jsonify({
        "id": user.id,
        "nom_utilisateur": user.nom_utilisateur,
        "photo_url": user.photo_url
    }), 200


@user_bp.put("/profil/mot_de_passe")
@jwt_required()
def update_password():
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    if not user:
        return jsonify({"error": "User pas trouvé"}), 404

    data = request.get_json() or {}
    old_password = data.get("ancien_mot_de_passe")
    new_password = data.get("nouveau_mot_de_passe")

    if not old_password or not new_password:
        return jsonify({"error": "Ancien et nouveau mot de passe requis"}), 400

    if not check_password_hash(user.mot_de_passe, old_password):
        return jsonify({"error": "Ancien mot de passe invalide"}), 400

    user.mot_de_passe = generate_password_hash(new_password)
    db.session.commit()

    return jsonify({"message": "Mot de passe mis à jour"}), 200


@user_bp.post("/profil/photo")
@jwt_required()
def upload_photo():
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    if not user:
        return jsonify({"error": "User pas trouvé"}), 404

    if "photo" not in request.files:
        return jsonify({"error": "Fichier 'photo' requis"}), 400

    file = request.files["photo"]
    if file.filename == "":
        return jsonify({"error": "Nom de fichier invalide"}), 400

    filename = secure_filename(file.filename)
    unique_name = f"user_{user.id}_{int(datetime.now().timestamp())}_{filename}"
    save_path = os.path.join(current_app.config["UPLOAD_FOLDER"], unique_name)
    file.save(save_path)

    user.photo_url = f"/uploads/{unique_name}"
    db.session.commit()

    return jsonify({"photo_url": user.photo_url}), 201


@user_bp.post("/suivre/<int:user_id>")
@jwt_required()
def suivre_utilisateur(user_id):
    current_user = int(get_jwt_identity())
    if current_user == user_id:
        return jsonify({"erreur": "Vous ne pouvez pas vous suivre"}), 400

    utilisateur_cible = User.query.get(user_id)
    if not utilisateur_cible:
        return jsonify({"error": "Utilisateur pas trouver"}), 404

    existing = Follow.query.filter_by(follower_id=current_user, following_id=user_id).first()
    if existing:
        return jsonify({"message": "Deja suivi"}), 404

    new_follow = Follow(follower_id=current_user, following_id=user_id)
    db.session.add(new_follow)
    db.session.commit()

    return jsonify({"message": "Followed successfully"}), 201

@user_bp.delete("/suivre/<int:user_id>")
@jwt_required()
def unfollow_user(user_id):
    current_user = get_jwt_identity()

    follow_rel = Follow.query.filter_by(follower_id=current_user, following_id=user_id).first()
    if not follow_rel:
        return jsonify({"message": "Pas suivi"}), 200

    db.session.delete(follow_rel)
    db.session.commit()

    return jsonify({"message": "Unfollowed successfully"}), 200
