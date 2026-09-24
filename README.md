# AutoClicker Flotante para Android

Esta aplicación implementa un botón flotante conmutable (**Play / Pausa**) que se superpone a cualquier juego o app en Android y realiza toques automáticos continuos en la pantalla con un intervalo configurable de **2 segundos (2000 ms)**.

---

## 🚀 Requisitos
- Android 7.0 (Nougat, API 24) o superior.
- **NO requiere ROOT**: utiliza la API nativa y segura `AccessibilityService` (`dispatchGesture`).

---

## 🛠️ Cómo compilar en Android Studio
1. Descarga el archivo ZIP del proyecto con el botón **"Descargar Proyecto Android (.zip)"**.
2. Descomprime la carpeta y abre **Android Studio**.
3. Selecciona **File > Open...** y elige la carpeta del proyecto.
4. Espera a que Gradle sincronice las dependencias.
5. Conecta tu teléfono Android por cable USB con la **Depuración USB** activada (o usa un emulador).
6. Haz clic en el botón verde **Run (▶)** para instalar la APK directamente.

---

## 📱 Permisos requeridos en tu teléfono
Al abrir la aplicación por primera vez:
1. **Mostrar sobre otras aplicaciones (Overlay)**:
   - Toca el botón en la app o ve a *Ajustes > Aplicaciones > AutoClicker > Mostrar sobre otras apps* y márcalo como Permitido.
2. **Servicio de Accesibilidad**:
   - Toca el botón para ir a *Ajustes > Accesibilidad > Servicios descargados / instalados*.
   - Busca **AutoClicker Flotante** y actívalo.
   - En dispositivos **Xiaomi / MIUI / HyperOS**: Asegúrate de deshabilitar la optimización MIUI para evitar que el sistema cierre el servicio en segundo plano.
   - En **Samsung OneUI**: Añade la app a la lista de "Aplicaciones que nunca se suspenden".

---

## 🎮 Cómo usarlo en tu juego
1. Abre tu juego favorito (RPG de toques, Cookie Clicker, Tap Titans, etc.).
2. Verás la burbuja flotante con el icono de **Play (▶)** y la **Mira azul de objetivo (⌖)**.
3. Arrastra la **mira azul** directamente sobre el botón, monstruo o elemento que necesitas golpear.
4. Arrastra la burbuja flotante a un lateral cómodo donde no te tape la vista.
5. Toca el botón **Play (▶)**: cambiará automáticamente al icono de **Pausa (⏸)** y comenzará a hacer toques exactamente cada **2 segundos**.
6. Para detenerlo en cualquier momento, vuelve a tocar el botón **Pausa (⏸)**.
