# Clean Card

App Android para diseñar tu propia "skin" de tarjeta y mostrarla a pantalla completa al pagar con el móvil.

## Cómo funciona

1. Diseñas la tarjeta: estilos (oro metálico, negro titanio, platino, holográfico, carbono…), colores, acabado, una foto tuya, nombre, etiqueta y últimos 4 dígitos decorativos.
2. Al pagar, abres tu diseño: tocando la tarjeta en la app, con el atajo del icono (mantén pulsado → «Pagar») o con el botón de Ajustes rápidos «Mi tarjeta».
3. Acercas el móvil **desbloqueado** al datáfono. **El pago lo hace Google Wallet** (tu app de pago predeterminada) en segundo plano con tu tarjeta real.

La app no paga, no usa el NFC y no guarda ningún dato bancario. Todo se guarda solo en el móvil.

## Limitaciones (sin adornos)

- **No modifica Google Wallet.** Ninguna app puede hacerlo: el diseño lo envía tu banco, Android aísla las apps entre sí y con root Google Wallet deja de funcionar.
- Al completar el pago, Google Wallet puede mostrar su propia confirmación con el diseño real de tu tarjeta.
- El datáfono y el ticket muestran la tarjeta y la red reales.
- La doble pulsación del botón de encendido sigue abriendo Google Wallet.
- No incluye diseños ni logos de marcas reales (American Express, Visa, Mastercard…): son marcas registradas. Puedes usar una imagen tuya para uso personal.

## Instalar

Cada push compila el APK en GitHub Actions: pestaña **Actions** → última ejecución → artefacto `clean-card-apk`. Descomprímelo en el móvil e instálalo (hay que permitir "instalar apps de origen desconocido"). Es un APK de depuración, no pasa por Google Play.

O ábrelo en Android Studio y ejecútalo.

Requisitos: Android 8.0 o superior. Para pagar, NFC activado y Google Wallet como app de pago predeterminada (Ajustes → Conexiones → NFC → Pagos sin contacto).
