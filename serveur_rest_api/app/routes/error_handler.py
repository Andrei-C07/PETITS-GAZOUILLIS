
@app.errorhandler(404)
def not_found(e):
    return jsonify({"error": "Ressource introuvable"}), 404