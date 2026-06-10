package com.kitchencabinet.i18n

/**
 * Centralized i18n (internationalization) system for the Kitchen Cabinet app.
 *
 * All user-facing strings are defined here as [StringKey] entries, mapped to
 * translations in supported locales ("es" by default, plus "en").
 *
 * Usage:
 *   I18n.get(StringKey.HOME)          → "Inicio"
 *   I18n.get(StringKey.HOME, "en")    → "Home"
 *   I18n.get(StringKey.DAYS_LEFT, args = "n" to 3) → "3 días restantes"
 */

// ────────────────────────────────────────────────────────────────────────────
// 1. StringKey – every translatable string in the app
// ────────────────────────────────────────────────────────────────────────────

enum class StringKey {
    // ── Navigation ──
    HOME,
    SEARCH,
    PANTRY,
    SHOPPING,
    FAVORITES,
    SETTINGS,
    TOOLS,

    // ── Common ──
    SAVE,
    CANCEL,
    DELETE,
    EDIT,
    ADD,
    CONFIRM,
    CLOSE,
    BACK,

    // ── Recipe ──
    WHAT_TO_COOK,
    SEARCH_RECIPES,
    NO_RECIPES,
    NO_RESULTS,
    COOK,
    COOK_MODE,
    INGREDIENTS,
    EQUIPMENT,
    STEPS,
    PREPARATION,
    SERVINGS,
    RATING,
    DIFFICULTY,
    MINUTES,
    CATEGORY,
    FAVORITE,
    DELETE_RECIPE,
    DELETE_CONFIRM,

    // ── Cook Mode ──
    STEP_X_OF_Y,
    PREVIOUS,
    NEXT,
    FINISH,
    TIMER,
    START,
    PAUSE,
    RESUME,
    RESET,
    SET_TIMER,
    MINUTES_SHORT,

    // ── Pantry ──
    MY_PANTRY,
    PANTRY_EMPTY,
    ADD_ITEM,
    MANAGE_CATEGORIES,
    EXPIRED,
    TODAY,
    TOMORROW,
    DAYS_LEFT,
    ITEMS,
    AVAILABLE,
    UNAVAILABLE,

    // ── Shopping ──
    SHOPPING_LIST,
    LIST_EMPTY,
    CLEAR_DONE,
    MOVE_TO_PANTRY,
    ADD_ITEM_SHORT,

    // ── Settings ──
    APPEARANCE,
    DARK_MODE,
    LIGHT_THEME,
    DARK_THEME,
    LANGUAGE,
    NOTIFICATIONS,
    EXPIRY_REMINDERS,
    EXPORT_BACKUP,
    IMPORT_BACKUP,
    NAVIGATION,

    // ── Tools ──
    UNIT_CONVERTER,
    SCALE_RECIPE,
    CONVERT,
    VALUE,

    // ── Meal Plan ──
    MEAL_PLAN,
    WEEK_OF,
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY,
    BREAKFAST,
    LUNCH,
    DINNER,
    SELECT_RECIPE,
    CLEAR_WEEK,
    ADD_ALL_TO_SHOPPING,

    // ── General ──
    COOKED_X_TIMES,
    SUBSTITUTES,
    NO_INGREDIENTS,

    // ── Search / Cook ──
    NO_STEPS,
    COOKABLE,
    ALMOST,
    OTHERS,
    ADD_MISSING,
    CLEAR,

    // ── Settings ──
    COPY,
    ENABLED,
    DISABLED,
    DATA,
    DAYS_BEFORE,
}

// ────────────────────────────────────────────────────────────────────────────
// 2. translations map – StringKey → (locale → translated text)
// ────────────────────────────────────────────────────────────────────────────

val translations: Map<StringKey, Map<String, String>> = run {
    val es = "es"
    val en = "en"

    mapOf(
        // ── Navigation ──
        StringKey.HOME to mapOf(es to "Inicio", en to "Home"),
        StringKey.SEARCH to mapOf(es to "Buscar", en to "Search"),
        StringKey.PANTRY to mapOf(es to "Despensa", en to "Pantry"),
        StringKey.SHOPPING to mapOf(es to "Compras", en to "Shopping"),
        StringKey.FAVORITES to mapOf(es to "Favoritos", en to "Favorites"),
        StringKey.SETTINGS to mapOf(es to "Ajustes", en to "Settings"),
        StringKey.TOOLS to mapOf(es to "Herramientas", en to "Tools"),

        // ── Common ──
        StringKey.SAVE to mapOf(es to "Guardar", en to "Save"),
        StringKey.CANCEL to mapOf(es to "Cancelar", en to "Cancel"),
        StringKey.DELETE to mapOf(es to "Eliminar", en to "Delete"),
        StringKey.EDIT to mapOf(es to "Editar", en to "Edit"),
        StringKey.ADD to mapOf(es to "Añadir", en to "Add"),
        StringKey.CONFIRM to mapOf(es to "Confirmar", en to "Confirm"),
        StringKey.CLOSE to mapOf(es to "Cerrar", en to "Close"),
        StringKey.BACK to mapOf(es to "Atrás", en to "Back"),

        // ── Recipe ──
        StringKey.WHAT_TO_COOK to mapOf(es to "¿Qué cocinar?", en to "What to cook?"),
        StringKey.SEARCH_RECIPES to mapOf(es to "Buscar recetas...", en to "Search recipes..."),
        StringKey.NO_RECIPES to mapOf(es to "Aún no hay recetas", en to "No recipes yet"),
        StringKey.NO_RESULTS to mapOf(es to "Sin resultados", en to "No results found"),
        StringKey.COOK to mapOf(es to "Cocinar", en to "Cook"),
        StringKey.COOK_MODE to mapOf(es to "Modo cocina", en to "Cook mode"),
        StringKey.INGREDIENTS to mapOf(es to "Ingredientes", en to "Ingredients"),
        StringKey.EQUIPMENT to mapOf(es to "Equipo", en to "Equipment"),
        StringKey.STEPS to mapOf(es to "Pasos", en to "Steps"),
        StringKey.PREPARATION to mapOf(es to "Preparación", en to "Preparation"),
        StringKey.SERVINGS to mapOf(es to "Porciones", en to "Servings"),
        StringKey.RATING to mapOf(es to "Puntuación", en to "Rating"),
        StringKey.DIFFICULTY to mapOf(es to "Dificultad", en to "Difficulty"),
        StringKey.MINUTES to mapOf(es to "min", en to "min"),
        StringKey.CATEGORY to mapOf(es to "Categoría", en to "Category"),
        StringKey.FAVORITE to mapOf(es to "Favorito", en to "Favorite"),
        StringKey.DELETE_RECIPE to mapOf(es to "Eliminar receta", en to "Delete recipe"),
        StringKey.DELETE_CONFIRM to mapOf(
            es to "¿Estás seguro? Esta acción no se puede deshacer.",
            en to "Are you sure? This cannot be undone."
        ),

        // ── Cook Mode ──
        StringKey.STEP_X_OF_Y to mapOf(es to "Paso {current} de {total}", en to "Step {current} of {total}"),
        StringKey.PREVIOUS to mapOf(es to "Anterior", en to "Previous"),
        StringKey.NEXT to mapOf(es to "Siguiente", en to "Next"),
        StringKey.FINISH to mapOf(es to "Finalizar", en to "Finish"),
        StringKey.TIMER to mapOf(es to "Temporizador", en to "Timer"),
        StringKey.START to mapOf(es to "Iniciar", en to "Start"),
        StringKey.PAUSE to mapOf(es to "Pausa", en to "Pause"),
        StringKey.RESET to mapOf(es to "Reiniciar", en to "Reset"),
        StringKey.RESUME to mapOf(es to "Reanudar", en to "Resume"),
        StringKey.SET_TIMER to mapOf(es to "Configurar temporizador", en to "Set timer"),
        StringKey.MINUTES_SHORT to mapOf(es to "min", en to "min"),

        // ── Pantry ──
        StringKey.MY_PANTRY to mapOf(es to "Mi Despensa", en to "My Pantry"),
        StringKey.PANTRY_EMPTY to mapOf(es to "Tu despensa está vacía", en to "Your pantry is empty"),
        StringKey.ADD_ITEM to mapOf(es to "Añadir artículo", en to "Add item"),
        StringKey.MANAGE_CATEGORIES to mapOf(es to "Gestionar categorías", en to "Manage categories"),
        StringKey.EXPIRED to mapOf(es to "Caducado", en to "Expired"),
        StringKey.TODAY to mapOf(es to "Hoy", en to "Today"),
        StringKey.TOMORROW to mapOf(es to "Mañana", en to "Tomorrow"),
        StringKey.DAYS_LEFT to mapOf(es to "{n} días restantes", en to "{n} days left"),
        StringKey.ITEMS to mapOf(es to "artículos", en to "items"),
        StringKey.AVAILABLE to mapOf(es to "Disponible", en to "Available"),
        StringKey.UNAVAILABLE to mapOf(es to "No disponible", en to "Unavailable"),

        // ── Shopping ──
        StringKey.SHOPPING_LIST to mapOf(es to "Lista de la compra", en to "Shopping list"),
        StringKey.LIST_EMPTY to mapOf(es to "Tu lista de compras está vacía", en to "Your shopping list is empty"),
        StringKey.CLEAR_DONE to mapOf(es to "Limpiar completados", en to "Clear done"),
        StringKey.MOVE_TO_PANTRY to mapOf(es to "Mover a despensa", en to "Move to pantry"),
        StringKey.ADD_ITEM_SHORT to mapOf(es to "Añadir", en to "Add"),

        // ── Settings ──
        StringKey.APPEARANCE to mapOf(es to "Apariencia", en to "Appearance"),
        StringKey.DARK_MODE to mapOf(es to "Modo oscuro", en to "Dark mode"),
        StringKey.LIGHT_THEME to mapOf(es to "Claro", en to "Light"),
        StringKey.DARK_THEME to mapOf(es to "Oscuro", en to "Dark"),
        StringKey.LANGUAGE to mapOf(es to "Idioma", en to "Language"),
        StringKey.NOTIFICATIONS to mapOf(es to "Notificaciones", en to "Notifications"),
        StringKey.EXPIRY_REMINDERS to mapOf(es to "Recordatorios de caducidad", en to "Expiry reminders"),
        StringKey.EXPORT_BACKUP to mapOf(es to "Exportar copia de seguridad", en to "Export backup"),
        StringKey.IMPORT_BACKUP to mapOf(es to "Importar copia de seguridad", en to "Import backup"),
        StringKey.NAVIGATION to mapOf(es to "Navegación", en to "Navigation"),

        // ── Tools ──
        StringKey.UNIT_CONVERTER to mapOf(es to "Conversor de unidades", en to "Unit converter"),
        StringKey.SCALE_RECIPE to mapOf(es to "Escalar receta", en to "Scale recipe"),
        StringKey.CONVERT to mapOf(es to "Convertir", en to "Convert"),
        StringKey.VALUE to mapOf(es to "Valor", en to "Value"),

        // ── Meal Plan ──
        StringKey.MEAL_PLAN to mapOf(es to "Plan de Comidas", en to "Meal Plan"),
        StringKey.WEEK_OF to mapOf(es to "Semana del {date}", en to "Week of {date}"),
        StringKey.MONDAY to mapOf(es to "Lunes", en to "Monday"),
        StringKey.TUESDAY to mapOf(es to "Martes", en to "Tuesday"),
        StringKey.WEDNESDAY to mapOf(es to "Miércoles", en to "Wednesday"),
        StringKey.THURSDAY to mapOf(es to "Jueves", en to "Thursday"),
        StringKey.FRIDAY to mapOf(es to "Viernes", en to "Friday"),
        StringKey.SATURDAY to mapOf(es to "Sábado", en to "Saturday"),
        StringKey.SUNDAY to mapOf(es to "Domingo", en to "Sunday"),
        StringKey.BREAKFAST to mapOf(es to "Desayuno", en to "Breakfast"),
        StringKey.LUNCH to mapOf(es to "Almuerzo", en to "Lunch"),
        StringKey.DINNER to mapOf(es to "Cena", en to "Dinner"),
        StringKey.SELECT_RECIPE to mapOf(es to "Seleccionar receta", en to "Select recipe"),
        StringKey.CLEAR_WEEK to mapOf(es to "Limpiar semana", en to "Clear week"),
        StringKey.ADD_ALL_TO_SHOPPING to mapOf(es to "Añadir todo a compras", en to "Add all to shopping"),

        // ── General ──
        StringKey.COOKED_X_TIMES to mapOf(es to "Cocinado {n} veces", en to "Cooked {n} times"),
        StringKey.SUBSTITUTES to mapOf(es to "Sustitutos", en to "Substitutes"),
        StringKey.NO_INGREDIENTS to mapOf(es to "Sin ingredientes", en to "No ingredients"),

        // ── Search / Cook ──
        StringKey.NO_STEPS to mapOf(es to "No hay pasos para esta receta.", en to "No steps for this recipe."),
        StringKey.COOKABLE to mapOf(es to "Cocinable", en to "Cookable"),
        StringKey.ALMOST to mapOf(es to "Casi", en to "Almost"),
        StringKey.OTHERS to mapOf(es to "Otros", en to "Others"),
        StringKey.ADD_MISSING to mapOf(es to "Añadir faltantes", en to "Add missing"),
        StringKey.CLEAR to mapOf(es to "Limpiar", en to "Clear"),

        // ── Settings ──
        StringKey.COPY to mapOf(es to "Copiar", en to "Copy"),
        StringKey.ENABLED to mapOf(es to "Activado", en to "Enabled"),
        StringKey.DISABLED to mapOf(es to "Desactivado", en to "Disabled"),
        StringKey.DATA to mapOf(es to "Datos", en to "Data"),
        StringKey.DAYS_BEFORE to mapOf(es to "Días antes", en to "Days before"),
    )
}

// ────────────────────────────────────────────────────────────────────────────
// 3. Top-level convenience function
// ────────────────────────────────────────────────────────────────────────────

/**
 * Returns the translation for [key] in the given [locale].
 * Defaults to Spanish ("es").
 *
 * If the translation is missing (e.g. locale not yet mapped), it falls back
 * to "en" and then to the enum name itself so the app never shows a blank.
 */
fun tt(key: StringKey, locale: String = "es"): String {
    val byLocale = translations[key]
        ?: return key.name

    return byLocale[locale]
        ?: byLocale["en"]
        ?: byLocale.values.firstOrNull()
        ?: key.name
}

// ────────────────────────────────────────────────────────────────────────────
// 4. I18n helper object
// ────────────────────────────────────────────────────────────────────────────

object I18n {

    /**
     * Returns the plain translation for [key] in the given [locale].
     *
     * Example:
     *   I18n.get(StringKey.HOME)      → "Inicio"
     *   I18n.get(StringKey.HOME, "en") → "Home"
     */
    fun get(key: StringKey, locale: String = "es"): String {
        return tt(key, locale)
    }

    /**
     * Returns the translation for [key] with named placeholders replaced by
     * the given [args].
     *
     * Placeholders in the translation string should be written as `{name}`.
     * Each [Pair] provides the placeholder name and its replacement value.
     *
     * Example:
     *   I18n.get(StringKey.DAYS_LEFT, args = "n" to 5)
     *     → "5 días restantes"
     *
     *   I18n.get(StringKey.STEP_X_OF_Y, args = "current" to 2, "total" to 5)
     *     → "Paso 2 de 5"
     */
    fun get(key: StringKey, locale: String = "es", vararg args: Pair<String, Any>): String {
        val template = tt(key, locale)
        if (args.isEmpty()) return template

        return args.fold(template) { acc, (name, value) ->
            acc.replace("{$name}", value.toString())
        }
    }
}
