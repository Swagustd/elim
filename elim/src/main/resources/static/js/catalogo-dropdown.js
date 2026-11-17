document.addEventListener('DOMContentLoaded', function () {
    const dropdown = document.getElementById('dropdownCatalogo');
    const btn = document.getElementById('btnCatalogo');

    if (btn && dropdown) {
        // Conmuta la visibilidad del menú al hacer clic en el botón
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            dropdown.classList.toggle('is-open');
        });

        // Cierra el menú si el usuario hace clic fuera de él
        document.addEventListener('click', function (e) {
            if (!dropdown.contains(e.target)) {
                dropdown.classList.remove('is-open');
            }
        });
    }
});