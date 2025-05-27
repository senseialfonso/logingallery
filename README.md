# LoginApp – Demo de Login y Galería en Java Swing

> **Propósito:** servir como punto de partida para que los estudiantes practiquen
> programación con Swing, manejo de colecciones en memoria y mejoras evolutivas.

---

## ✨ Características actuales

| Módulo | Descripción |
|---|---|
| **Login** | Autentica usuarios **en memoria** (sin BD) con contraseñas encriptadas (SHA-256). |
| **Usuario _admin_** | Existe por defecto (`admin / admin`). Solo él ve la pestaña **Usuarios** para crear/modificar cuentas y otorgar rol de administrador. |
| **Galería personal** | Cada usuario puede subir hasta **5 imágenes** y asignarles una descripción. Las imágenes se muestran como miniaturas. |
| **Criptografía** | Implementada con `java.security.MessageDigest`. No se almacenan contraseñas en texto plano. |

---

## 🛠️ Requisitos

- **JDK 17** o superior (probado hasta Java 21).
- Maven <sup>(recomendado)</sup> o tu IDE favorito (NetBeans, IntelliJ, VS Code).

---

## 🚀 Ejecución rápida (Maven)

```bash
mvn clean compile exec:java \
  -Dexec.mainClass="com.mycompany.loginapp.LoginApp"
```

Abre la ventana de inicio de sesión → ingresa `admin` / `admin`.

---

## 🧑‍🎓 Tareas para los alumnos

1. **Cerrar sesión**  
   - Añadir un botón visible que permita volver a la pantalla de login sin finalizar la aplicación.  
   - Limpiar campos y estados antes de regresar.

2. **Mostrar la descripción de cada imagen en la galería**  
   - Al hacer clic (o con tooltip) mostrar la descripción. O bien colocarla como subtítulo bajo la miniatura.

3. **Video de presentación (máx. 3 minutos)**  
   - Demostrar el flujo completo (login, cierre de sesión, descripciones).  
   - Explicar brevemente los cambios de código implementados.

---

## 📝 Guía de estilo recomendada

- **Paquetes**: usa tu propio dominio (e.g. `mx.escuela.nombre.loginapp`).
- **Nombres**: clases en *PascalCase*, variables en *camelCase*.
- **Swing**: evita código duplicado; encapsula lógica repetida.
- **Commits**: pequeños y descriptivos.
