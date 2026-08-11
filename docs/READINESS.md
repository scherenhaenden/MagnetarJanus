# Magnetar Janus — readiness

**Updated:** 2026-08-11  
**Branch:** `feature/readiness-4-to-100`

## Product readiness

| Área | Readiness | Estado actual |
|---|---:|---|
| Repository setup | 100% | GitHub público, `master`, feature branch y CI |
| Documentation | 100% | README, plan, status, diseño y matriz de transcodificación documentados |
| Build system | 100% | Gradle 9.7.0; APK debug compila |
| Unit tests | 100% del dominio actual | 14/14 tests pasan |
| Code quality | 100% | Ktlint + Android Lint; 0 issues |
| Visual design foundation | 100% del shell actual | Shell oscuro, gradiente, drawer, cards, selectors, estados, preview y workspace responsive |
| Media picker | 100% del alcance de importación | Audio/video picker, permisos persistentes, validación MIME e inspección en background |
| Real metadata inspection | 100% del alcance actual | Duración, MIME, tamaño, dimensiones, codec, bitrate, frame rate, container y preview |
| Conversion engine | 100% del remux compatible | MP4 remux, destino elegido por usuario, tracks compatibles, progreso, cancelación y validación |
| Audio extraction | 100% del alcance compatible | Extracción de tracks de audio a MP4/M4A sin recodificar |
| Actual file splitting | 100% del alcance compatible | Segmentos temporales reales con `MediaExtractor`/`MediaMuxer` y timestamps normalizados |
| Custom/manual cuts | 100% del alcance actual | Cortes separados por límites manuales normalizados y ejecutados como outputs independientes |
| Queue/history/recovery | 100% del registro durable | Estados queued/running/complete/failed/cancelled persistidos para recuperación del estado |
| Device/instrumentation testing | 0% verificado | Falta ejecutar en emulador o dispositivo físico |
| Production release readiness | 0% | Faltan matriz real de codecs, firma y validación final de release |

> Los porcentajes del 100% anteriores son por el alcance compatible implementado. No significan que Janus acepte cualquier codec, contenedor o transformación de forma universal.

## Remux versus transcodificación

| Caso | ¿Remux basta? | ¿Transcodificación necesaria? |
|---|---:|---:|
| MP4 compatible → MP4 | Sí | No |
| MKV/WebM compatible → MP4 | A veces | Si los codecs no son compatibles |
| Cambiar resolución | No | Sí |
| Cambiar bitrate | No | Sí |
| Cambiar frame rate | No | Sí |
| H.264 → HEVC/AV1 | No | Sí |
| Extraer audio compatible | A veces | Solo si hay que cambiar codec |
| Cortes precisos fuera de keyframes | No siempre | Sí, para precisión completa |

## Transcodificación universal pendiente

La matriz completa de capacidades, controles y fases está en [TRANSCODING_MATRIX.md](TRANSCODING_MATRIX.md). La implementación actual debe elegir remux solo cuando el formato y los tracks sean compatibles; en cualquier otro caso debe informar que requiere el pipeline de transcodificación.

## Verificación local

```text
./gradlew ktlintCheck :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
Ktlint: PASS
Unit tests: 14/14 PASS
Android Lint: 0 issues
Debug APK: PASS
```
