from flask_seeder import Seeder
from app.models import User, Publication
from app.db import db
from datetime import datetime, timezone
from werkzeug.security import generate_password_hash

class DefaultSeeder(Seeder):
    def run(self):
        print("Seeding database...")

        db.session.query(Publication).delete()
        db.session.query(User).delete()

        user1 = User(
            nom_utilisateur="alex",
            mot_de_passe=generate_password_hash("password1"),
            created_at=datetime.now(timezone.utc)
        )
        user2 = User(
            nom_utilisateur="andrei",
            mot_de_passe=generate_password_hash("password12"),
            created_at=datetime.now(timezone.utc)
        )

        db.session.add_all([user1, user2])
        db.session.commit()

        pub1 = Publication(content="Salut, premier post test", user_id=user1.id)
        pub2 = Publication(content="Salut, deuxieme post test - User 2", user_id=user2.id)

        db.session.add_all([pub1, pub2])
        db.session.commit()

        print("Fin du seending process")