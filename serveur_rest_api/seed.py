from app import create_app
from app.db import db
from app.seeds.seeds import DefaultSeeder

app = create_app()

if __name__ == "__main__":
    print("seeding database...")
    with app.app_context():
        seeder = DefaultSeeder()
        seeder.db = db
        seeder.run()
    print("Seeding complete!")
