from flask import Blueprint, jsonify
from flask_jwt_extended import jwt_required, get_jwt_identity
from app.models import User, Follow
from app.db import db

user_bp = Blueprint("user_bp", __name__)


@user_bp.get("/")
@jwt_required()
def list_users():
    utilisateurs = User.query.all()
    result = [
        {
            "id": u.id,
            "nom_utilisateur": u.nom_utilisateur,
            "created_at": u.created_at.isoformat(),
        }
        for u in utilisateurs
    ]
    return jsonify(result), 200


@user_bp.get("/<int:user_id>")
@jwt_required()
def get_user(user_id):
    user = User.query.get(user_id)
    if not user:
        return jsonify({"error": "User pas trouver"}), 404

    current_user_id = get_jwt_identity()

    followers_count = Follow.query.filter_by(following_id=user.id).count()
    following_count = Follow.query.filter_by(follower_id=user.id).count()
    is_following = Follow.query.filter_by(
        follower_id=current_user_id,
        following_id=user.id
    ).first() is not None

    result = {
        "id": user.id,
        "nom_utilisateur": user.nom_utilisateur,
        "created_at": user.created_at.isoformat(),
        "followers_count": followers_count,
        "following_count": following_count,
        "is_following": is_following
    }
    return jsonify(result), 200

@user_bp.get("/current_user")
@jwt_required()
def get_current_user():
    current_user_id = get_jwt_identity()
    user = User.query.get(current_user_id)
    if not user:
        return jsonify({"error": "User pas trouvé"}), 404

    followers_count = Follow.query.filter_by(following_id=user.id).count()
    following_count = Follow.query.filter_by(follower_id=user.id).count()
    result = {
        "id": user.id,
        "nom_utilisateur": user.nom_utilisateur,
        "created_at": user.created_at.isoformat(),
        "followers_count": followers_count,
        "following_count": following_count,
    }
    return jsonify(result), 200


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