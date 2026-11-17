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

        // Ocultar todos los grupos
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

    function recalcalcularStock() {
        let total = 0;
        // Sumar únicamente los inputs de las tallas que están visibles en pantalla
        const inputsVisibles = document.querySelectorAll(".grupo-tallas:not([style*='display: none']) .input-talla");
        inputsVisibles.forEach(input => {
            const val = parseInt(input.value) || 0;
            total += val;
        });

        if (inputStockTotal) {
            inputStockTotal.value = total;
        }
    }

    // Ejecutar al cargar la página para que muestre las tallas del producto existente
    actualizarTallasPorCategoria();

    // Escuchar cambios de categoría
    selectCategoria.addEventListener("change", actualizarTallasPorCategoria);

    // Escuchar cambios en los inputs de tallas para recalcular el stock total
    document.querySelectorAll(".input-talla").forEach(input => {
        input.addEventListener("input", recalcularStock);
    });
});