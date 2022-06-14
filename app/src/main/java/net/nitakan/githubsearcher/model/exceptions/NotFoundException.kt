package net.nitakan.githubsearcher.model.exceptions

class NotFoundException(override val message: String?, override val cause: Throwable? = null) :
    Exception()