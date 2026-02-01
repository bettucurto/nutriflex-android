package ui.registration

import components.Gender

sealed class RegisterUIEvent {

    data class RegisterNameChanged(val name:String) : RegisterUIEvent()

    data class RegisterGenderChanged(val gender: Gender) : RegisterUIEvent()

    data class RegisterBirthDateChanged(val date: String) : RegisterUIEvent()

    data class RegisterGoalChanged(val goal: Int) : RegisterUIEvent()

    data class RegisterDifficultyChanged(val difficulty: Int) : RegisterUIEvent()

    data class RegisterHeightChanged(val height: Int) : RegisterUIEvent()

    data class RegisterWeightChanged(val weight: Float) : RegisterUIEvent()

    data class RegisterWeightGoalChanged(val weightGoal: Float) : RegisterUIEvent()

    data class ToggleAutoWeightGoal(val enabled: Boolean) : RegisterUIEvent()

    data class RegisterEmailChanged(val email:String) : RegisterUIEvent()

    data class RegisterPasswordChanged(val password:String) : RegisterUIEvent()

    data class RegisterActivityChanged(val activityLevel: Int): RegisterUIEvent()

    object NextClickedStep1 : RegisterUIEvent()

    object NextClickedStep2 : RegisterUIEvent()

    object NextClickedStep3 : RegisterUIEvent()

    object NextClickedStep4 : RegisterUIEvent()

    object NextClickedStep5 : RegisterUIEvent()

    object NextClickedStep6 : RegisterUIEvent()


}