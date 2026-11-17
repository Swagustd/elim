document.addEventListener("DOMContentLoaded", function () {
    // Escuchar clics en todos los botones de eliminar usuario
    const botonesEliminar = document.querySelectorAll(".btn-eliminar-usuario");

    botonesEliminar.forEach(boton => {
        boton.addEventListener("click", function (e) {
            e.preventDefault(); // Prevenir cualquier acción por defecto del formulario

            const documento = this.getAttribute("data-doc");
            const nombre = this.getAttribute("data-nombre");

            if (confirm("⚠️ ¿Estás seguro de que deseas eliminar permanentemente al usuario '" + nombre)) {
                window.location.href = "/admin/eliminar-usuario/" + documento;
            }
        });
    });
});