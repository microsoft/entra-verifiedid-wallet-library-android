package com.microsoft.walletlibrary.requests.requirements

enum class GroupRequirementOperator {
    ANY,
    ALL
}

class GroupRequirement: Requirement {
    override var required: Boolean = false

    val requirements = mutableListOf<Requirement>()

    var requirementOperator = GroupRequirementOperator.ANY

    override fun validate() {
        TODO("Not yet implemented")
    }
}