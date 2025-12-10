package com.championsita.red;

/**
 * Interfaz para sincronizar cualquier pantalla del lobby online.
 * El HiloCliente llama a estos métodos cuando llegan eventos del servidor.
 *
 * Cada pantalla implementa SOLO lo que necesita; los demás pueden quedar vacíos.
 */
public interface LobbySync {

    // -------------------------
    // SINCRONIZACIÓN DE SKINS
    // -------------------------
    default void aplicarSkinRival(String skin) {}

    // READY en selección de skin
    void aplicarReadyRival(boolean listo);

    // -------------------------
    // CONFIGURACIÓN DE PARTIDA
    // -------------------------
    default void aplicarCampoRival(String campo) {}

    default void aplicarGolesRival(int goles) {}

    default void aplicarTiempoRival(int tiempo) {}

    default void aplicarModoRival(String modo) {}

    void actualizarIndiceSkinRival(String substring);
}
