package com.diagonal.cordeis.view;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Gerenciador de atalhos de teclado para o sistema
 * Fornece atalhos consistentes em toda a aplicação
 */
@Slf4j
public class KeyboardShortcutManager {

    private final Scene scene;
    private final Map<KeyCombination, Runnable> shortcuts = new HashMap<>();

    public KeyboardShortcutManager(Scene scene) {
        this.scene = scene;
        setupGlobalShortcuts();
    }

    /**
     * Configura atalhos globais padrão do sistema
     */
    private void setupGlobalShortcuts() {
        // Atalhos básicos já implementados pelo sistema
        log.debug("Gerenciador de atalhos inicializado");
    }

    /**
     * Adiciona um atalho de teclado
     */
    public void addShortcut(KeyCombination combination, Runnable action) {
        shortcuts.put(combination, action);

        scene.getAccelerators().put(combination, action);
        log.debug("Atalho adicionado: {}", combination.getDisplayText());
    }

    /**
     * Remove um atalho de teclado
     */
    public void removeShortcut(KeyCombination combination) {
        shortcuts.remove(combination);
        scene.getAccelerators().remove(combination);
        log.debug("Atalho removido: {}", combination.getDisplayText());
    }

    /**
     * Adiciona atalhos comuns para um contexto específico
     */
    public void addCommonShortcuts(ShortcutContext context) {
        switch (context) {
            case FORM_EDITING:
                addFormEditingShortcuts();
                break;
            case TABLE_NAVIGATION:
                addTableNavigationShortcuts();
                break;
            case MODAL_DIALOG:
                addModalDialogShortcuts();
                break;
        }
    }

    /**
     * Atalhos para edição de formulários
     */
    private void addFormEditingShortcuts() {
        // Ctrl+S para salvar
        addShortcut(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                () -> log.debug("Atalho Salvar acionado"));

        // Ctrl+N para novo
        addShortcut(new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                () -> log.debug("Atalho Novo acionado"));

        // Ctrl+Z para desfazer
        addShortcut(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN),
                () -> log.debug("Atalho Desfazer acionado"));

        // Escape para cancelar
        addShortcut(new KeyCodeCombination(KeyCode.ESCAPE),
                () -> log.debug("Atalho Cancelar acionado"));
    }

    /**
     * Atalhos para navegação em tabelas
     */
    private void addTableNavigationShortcuts() {
        // F5 para atualizar
        addShortcut(new KeyCodeCombination(KeyCode.F5),
                () -> log.debug("Atalho Atualizar acionado"));

        // Delete para excluir
        addShortcut(new KeyCodeCombination(KeyCode.DELETE),
                () -> log.debug("Atalho Excluir acionado"));

        // F2 para editar
        addShortcut(new KeyCodeCombination(KeyCode.F2),
                () -> log.debug("Atalho Editar acionado"));
    }

    /**
     * Atalhos para diálogos modais
     */
    private void addModalDialogShortcuts() {
        // Enter para confirmar
        addShortcut(new KeyCodeCombination(KeyCode.ENTER),
                () -> log.debug("Atalho Confirmar acionado"));

        // Escape para cancelar
        addShortcut(new KeyCodeCombination(KeyCode.ESCAPE),
                () -> log.debug("Atalho Cancelar modal acionado"));
    }

    /**
     * Cria atalhos específicos para componentes
     */
    public static void addComponentShortcuts(Node component, ComponentShortcuts shortcuts) {
        component.setOnKeyPressed(event -> {
            KeyCombination pressed = new KeyCodeCombination(event.getCode(),
                    event.isControlDown() ? KeyCombination.CONTROL_DOWN : KeyCombination.CONTROL_ANY,
                    event.isShiftDown() ? KeyCombination.SHIFT_DOWN : KeyCombination.SHIFT_ANY,
                    event.isAltDown() ? KeyCombination.ALT_DOWN : KeyCombination.ALT_ANY);

            if (shortcuts.getShortcuts().containsKey(pressed)) {
                shortcuts.getShortcuts().get(pressed).run();
                event.consume();
            }
        });
    }

    /**
     * Limpa todos os atalhos
     */
    public void clearShortcuts() {
        scene.getAccelerators().clear();
        shortcuts.clear();
        log.debug("Todos os atalhos foram removidos");
    }

    /**
     * Obtém todos os atalhos registrados
     */
    public Map<KeyCombination, Runnable> getShortcuts() {
        return new HashMap<>(shortcuts);
    }

    /**
     * Contextos predefinidos para atalhos
     */
    public enum ShortcutContext {
        FORM_EDITING,
        TABLE_NAVIGATION,
        MODAL_DIALOG
    }

    /**
     * Interface para atalhos de componentes específicos
     */
    public static class ComponentShortcuts {
        private final Map<KeyCombination, Runnable> shortcuts = new HashMap<>();

        public void add(KeyCode key, Runnable action) {
            shortcuts.put(new KeyCodeCombination(key), action);
        }

        public void add(KeyCode key, KeyCombination.Modifier modifier, Runnable action) {
            shortcuts.put(new KeyCodeCombination(key, modifier), action);
        }

        public Map<KeyCombination, Runnable> getShortcuts() {
            return shortcuts;
        }
    }
}
