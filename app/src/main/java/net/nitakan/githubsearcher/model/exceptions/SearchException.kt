package net.nitakan.githubsearcher.model.exceptions

class SearchException(
    override val message: String?,
    override val cause: Throwable? = null
) : Exception()