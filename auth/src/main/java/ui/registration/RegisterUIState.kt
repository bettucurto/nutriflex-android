package ui.registration

import components.Gender

data class RegisterUIState (
    var email: String = "",
    val emailError: String? = null,
    var password: String = "",
    val passwordError: String? = null,
    var name: String = "",
    val nameError: String? = null,
    val gender: Gender? = null,
    val genderError: String? = null,
    val birthDate: String = "",
    val birthDateError: String? = null,
    val isStep1Valid: Boolean = false,
    val goal: Int? = null,
    val goalError: String? = null,
    val difficulty: Int? = null,
    val difficultyError: String? = null,
    val height: Int? = null,
    val heightError: String? = null,
    val weight: Float? = null,
    val weightError: String? = null,
    val weightGoal: Float? = null,
    val weightGoalError: String? = null,
    val autoWeightGoal: Boolean = false,
    val isStep4Valid: Boolean = false,
    val registerError: String? = null
)

