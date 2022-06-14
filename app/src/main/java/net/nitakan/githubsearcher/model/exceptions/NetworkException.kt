package net.nitakan.githubsearcher.model.exceptions

class NetworkException(
    override val message: String?,
    override val cause: Throwable?
) : Exception()