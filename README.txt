Paolo AutoSell Singleplayer - Fabric 1.20.4

Qué hace:
- Funciona solo en mundo de un jugador/integrado.
- Presiona O para iniciar o pausar.
- Presiona P para reiniciar el ciclo.
- Solo ejecuta comandos si hay un item en la segunda mano.
- Ejecuta /sellall con tiempos aleatorios tipo 4.789, 3.190, 6.123, 5.545.
- No repite el entero 3/4/5/6 hasta terminar el ciclo de esos 4 números.
- No repite las milésimas hasta agotar la bolsa de 001 a 999.
- Cada 5 /sellall programa /home up después de 1.xxx a 3.xxx segundos.
- Corre 15 min, pausa aleatoria 2.8 a 3.2 min, corre 10 min, pausa, corre 13 min, pausa y repite.

Cómo compilar:
1. Instala JDK 17.
2. Abre esta carpeta con IntelliJ IDEA o VS Code.
3. Ejecuta: gradle build
4. El .jar saldrá en: build/libs/
5. Copia el .jar a la carpeta mods de Minecraft junto con Fabric API para 1.20.4.

Nota:
No está pensado para servidores públicos ni para saltarse reglas de servidores. Está limitado a singleplayer.
