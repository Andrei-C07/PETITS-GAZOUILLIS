from flask_jwt_extended import create_access_token
from werkzeug.security import check_password_hash
from app.models import User


def auth_user(nom_utilisateur, mdp):
    user = User.query.filter_by(nom_utilisateur=nom_utilisateur).first()
    if not user:
        return None

    if not check_password_hash(user.mot_de_passe, mdp):
        return None
    # Retourne un JWT token
    return create_access_token(identity=str(user.id))
