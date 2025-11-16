from flask import Flask, render_template

app = Flask(
    __name__,
    static_folder="../frontend/static",
    template_folder="../frontend/templates",
)

@app.route("/")
def index():
    return render_template("index.html")  # served from frontend/templates

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
