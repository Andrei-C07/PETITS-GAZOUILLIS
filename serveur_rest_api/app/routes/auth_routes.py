from flask import Blueprint, json, request, jsonify
from app.services.auth_service import auth_user
from app.models import User
from flask_jwt_extended import jwt_required

auth_bp = Blueprint("auth_bp", __name__)


@auth_bp.post("/")
def login():
    data = request.json

    nom_utilisateur = data.get("nom_utilisateur")
    mdp = data.get("mot_de_passe")
    user = User.query.filter_by(nom_utilisateur=nom_utilisateur).first()

    if not nom_utilisateur or not mdp:
        return jsonify({"error": "nom_utilisateur ou mot de passe est requis."}), 400

    token = auth_user(nom_utilisateur, mdp)

    if not token:
        return jsonify({"error": "Info pas valide"}), 401

    return jsonify({"token": token, "user_id": user.id }), 200

@auth_bp.post("deconnexion")
@jwt_required()
def logout():
    return jsonify({"message": "Deconnecter avec succes"}), 200
