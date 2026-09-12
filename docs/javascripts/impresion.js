/* DWES · Utilidades de impresión
   1) Despliega todas las soluciones (<details>) antes de imprimir y las vuelve
      a cerrar después, para que el PDF salga completo sin cambiar la vista web.
   2) En la página imprimible, las abre directamente al cargar.
   3) Añade un botón 🖨 en la barra superior que lleva a la página imprimible.   */

(function () {
  "use strict";

  const RUTA_IMPRIMIBLE = "curso-completo";

  /* --- 1. Abrir / cerrar <details> alrededor de la impresión --- */
  function abrirTodo() {
    document.querySelectorAll("details:not([open])").forEach(function (d) {
      d.setAttribute("open", "");
      d.dataset.dwesAuto = "1";
    });
  }
  function restaurar() {
    document.querySelectorAll("details[data-dwes-auto]").forEach(function (d) {
      d.removeAttribute("open");
      delete d.dataset.dwesAuto;
    });
  }
  window.addEventListener("beforeprint", abrirTodo);
  window.addEventListener("afterprint", restaurar);

  /* Safari y algunos navegadores no lanzan beforeprint: usamos también matchMedia */
  if (window.matchMedia) {
    const mm = window.matchMedia("print");
    const cb = function (e) { e.matches ? abrirTodo() : restaurar(); };
    mm.addEventListener ? mm.addEventListener("change", cb) : mm.addListener(cb);
  }

  document.addEventListener("DOMContentLoaded", function () {

    const esImprimible = location.pathname.indexOf(RUTA_IMPRIMIBLE) !== -1;

    /* --- 2. En la página imprimible, todo desplegado desde el principio --- */
    if (esImprimible) {
      document.querySelectorAll("details").forEach(function (d) {
        d.setAttribute("open", "");
      });
    }

    /* --- 3. Botón de impresión en la barra superior --- */
    const barra = document.querySelector(".md-header__inner .md-header__option") ||
                  document.querySelector(".md-header__inner");
    if (!barra) return;

    const boton = document.createElement(esImprimible ? "button" : "a");
    boton.className = "md-header__button md-icon dwes-print";
    boton.title = esImprimible
      ? "Imprimir o guardar como PDF (Ctrl+P)"
      : "Versión imprimible del curso completo";
    boton.setAttribute("aria-label", boton.title);
    boton.innerHTML =
      '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="24" height="24">' +
      '<path fill="currentColor" d="M18 3H6v4h12m1 5a1 1 0 0 1-1-1 1 1 0 0 1 1-1 1 1 0 0 1 1 1' +
      ' 1 1 0 0 1-1 1m-3 7H8v-5h8m3-6H5a3 3 0 0 0-3 3v6h4v4h12v-4h4v-6a3 3 0 0 0-3-3Z"/></svg>';

    if (esImprimible) {
      boton.addEventListener("click", function () { window.print(); });
    } else {
      const base = document.querySelector('link[rel="canonical"]');
      const raiz = (window.location.pathname.match(/^(.*?)(\/[^/]*\/?)?$/) || [])[0];
      boton.href = (document.querySelector('.md-header__button.md-logo') || {}).href
                   ? document.querySelector('.md-header__button.md-logo').href.replace(/\/?$/, "/") + RUTA_IMPRIMIBLE + "/"
                   : "/" + RUTA_IMPRIMIBLE + "/";
    }

    barra.parentNode.insertBefore(boton, barra);
  });
})();
