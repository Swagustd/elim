document.addEventListener("DOMContentLoaded", function () {
    const selectCategoria = document.getElementById("id_categoria_fk");
    const seccionTallas = document.getElementById("seccion_tallas");
    const gruposTallas = document.querySelectorAll(".grupo-tallas");
    const inputStockTotal = document.getElementById("stock");

    function actualizarTallasPorCategoria() {
        const optionSeleccionada = selectCategoria.options[selectCategoria.selectedIndex];
        const nombreCategoria = optionSeleccionada ? (optionSeleccionada.getAttribute("data-nombre") || optionSeleccionada.text).trim() : "";

        if (!nombreCategoria || selectCategoria.value === "") {
            seccionTallas.style.display = "none";
            recalcularStock();
            return;
        }

        seccionTallas.style.display = "block";

        // Ocultar todos los grupos y resetear sus valores no visibles
        gruposTallas.forEach(grupo => {
            grupo.style.display = "none";
        });

        let grupoActivo = document.getElementById("grupo_adultos");

        if (nombreCategoria.toLowerCase().includes("bebé") || nombreCategoria.toLowerCase().includes("bebe")) {
            grupoActivo = document.getElementById("grupo_bebes");
        } else if (nombreCategoria.toLowerCase().includes("niño") || nombreCategoria.toLowerCase().includes("nino")) {
            grupoActivo = document.getElementById("grupo_ninos");
        }

        if (grupoActivo) {
            grupoActivo.style.display = "grid";
        }

        recalcularStock();
    }

    function recalcularStock() {
        let total = 0;
        // Solo sumar los inputs del grupo de tallas visible
        const inputsVisibles = document.querySelectorAll(".grupo-tallas:not([style*='display: none']) .input-talla");
        inputsVisibles.forEach(input => {
            const val = parseInt(input.value) || 0;
            total += val;
        });

        if (inputStockTotal) {
            inputStockTotal.value = total;
        }
    }

    selectCategoria.addEventListener("change", actualizarTallasPorCategoria);

    document.querySelectorAll(".input-talla").forEach(input => {
        input.addEventListener("input", recalcularStock);
    });
});