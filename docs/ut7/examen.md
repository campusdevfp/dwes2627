# Preparar el examen — UT7

**:material-laptop: Examen práctico · 100 % · 2 sesiones**

## Formato

Se te entrega una API **abierta** y funcionando. Tienes que protegerla y añadirle gestión de estado.

## Qué se pide

1. Usuarios en memoria con contraseñas **hasheadas** y tres roles.
2. `POST /auth/login` que devuelva un JWT, y filtro que lo valide.
3. Matriz de permisos por método y rol, cerrada por defecto.
4. Refresco revocable y token de acceso corto.
5. Un endpoint de preferencias con **cookie** bien configurada.
6. Estado de usuario en **sesión** para la parte web.
7. Al menos cinco tests, incluidos 401 y 403.

## Rúbrica

| Criterio | Peso |
|---|---:|
| 1. Arranca y responde *(eliminatorio)* | 10 % |
| 2. Contraseñas hasheadas y usuarios con roles | 20 % |
| 3. Login JWT y filtro de validación | 20 % |
| 4. Matriz de permisos correcta (401 vs 403) | 25 % |
| 5. Sesión y cookie con sus atributos | 15 % |
| 6. Tests de seguridad | 10 % |

## Errores que más cuestan

1. **Contraseñas sin hashear** → criterio 2 a cero, aunque todo lo demás sea perfecto.
2. `.anyRequest().permitAll()` arriba de la cadena → la API queda abierta.
3. `roles = "ROLE_ADMIN"` → genera `ROLE_ROLE_ADMIN` y un 403 incomprensible.
4. Confundir **401** (no sé quién eres) con **403** (sé quién eres y no puedes).
5. Cookie de sesión sin `HttpOnly`.
6. Secreto del JWT en el código en vez de en variable de entorno.
7. Mensaje de login que distingue «usuario no existe» de «contraseña incorrecta».
8. `@WebMvcTest` sin `@Import(SecurityConfig.class)`: los tests de 401/403 no prueban nada.
