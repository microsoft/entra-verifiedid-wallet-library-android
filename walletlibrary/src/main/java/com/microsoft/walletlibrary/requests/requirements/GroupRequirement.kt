package com.microsoft.walletlibrary.requests.requirements

enum class GroupRequirementOperator {
    ANY,
    ALL
}

class GroupRequirement(
    override val required: Boolean,
    val requirements: List<Requirement>,
    val requirementOperator: GroupRequirementOperator
): Requirement {

    override fun validate() {
        TODO("Not yet implemented")
    }
}