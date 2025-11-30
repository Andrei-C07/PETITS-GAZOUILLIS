from datetime import datetime, timezone
from .db import db


class User(db.Model):
    __tablename__ = "users"

    id = db.Column(db.Integer, primary_key=True)
    nom_utilisateur = db.Column(db.String(60), unique=True, nullable=False)
    mot_de_passe = db.Column(db.String(255), nullable=False)
    photo_url = db.Column(db.String(255))
    created_at = db.Column(db.DateTime, default=datetime.now(timezone.utc))

    publications = db.relationship("Publication", backref="user", lazy=True)

    followers = db.relationship(
        "Follow",
        foreign_keys="Follow.following_id",
        backref="followed_user",
        lazy="dynamic",
    )

    following = db.relationship(
        "Follow",
        foreign_keys="Follow.follower_id",
        backref="follower_user",
        lazy="dynamic",
    )


class Publication(db.Model):
    __tablename__ = "publications"

    id = db.Column(db.Integer, primary_key=True)
    content = db.Column(db.String(280), nullable=False)
    created_at = db.Column(db.DateTime, default=datetime.now(timezone.utc))

    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)


class Follow(db.Model):
    __tablename__ = "follows"

    id = db.Column(db.Integer, primary_key=True)

    # User qui follow quelquun
    follower_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)

    # User qui se fait follow par quelquun dautre
    following_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)

    created_at = db.Column(db.DateTime, default=datetime.now(timezone.utc))
