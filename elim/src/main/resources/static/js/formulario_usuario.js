document.addEventListener("DOMContentLoaded", function () {
    // 1. Mostrar / Ocultar contraseña
    const toggleButtons = document.querySelectorAll(".toggle-password");

    toggleButtons.forEach(button => {
        button.addEventListener("click", function () {
            const targetId = this.getAttribute("data-target");
            const input = document.getElementById(targetId);

            if (input.type === "password") {
                input.type = "text";
                this.classList.remove("fa-eye");
                this.classList.add("fa-eye-slash");
            } else {
                input.type = "password";
                this.classList.remove("fa-eye-slash");
                this.classList.add("fa-eye");
            }
        });
    });

    // 2. Validar coincidencia y fortaleza de contraseñas
    const form = document.getElementById("userForm");
    const password = document.getElementById("password");
    const confirmPassword = document.getElementById("confirmPassword");
    const passwordError = document.getElementById("passwordError");

    // Expresión regular: Mínimo 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial
    const strongPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&.#_-])[A-Za-z\d@$!%*?&.#_-]{8,}$/;

    form.addEventListener("submit", function (e) {
        let hasError = false;

        // Validar contraseña segura
        if (!strongPasswordRegex.test(password.value)) {
            e.preventDefault();
            alert("La contraseña debe tener al menos 8 caracteres, incluir una mayúscula, una minúscula, un número y un carácter especial (@$!%*?&.#_-).");
            password.focus();
            hasError = true;
            return;
        }

        // Validar coincidencia
        if (password.value !== confirmPassword.value) {
            e.preventDefault();
            passwordError.innerText = "Las contraseñas no coinciden";
            passwordError.style.display = "block";
            confirmPassword.style.borderColor = "#e53e3e";
            confirmPassword.focus();
            hasError = true;
        }
    });

    confirmPassword.addEventListener("input", function () {
        if (password.value === confirmPassword.value) {
            passwordError.style.display = "none";
            confirmPassword.style.borderColor = "#e2e8f0";
        }
    });
});