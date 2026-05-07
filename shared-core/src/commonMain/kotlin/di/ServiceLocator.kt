package com.kmptv.shared_core.di

import com.kmptv.shared_core.repositories.ContentRepository
import com.kmptv.shared_core.repositories.ContentRepositoryImpl
import com.kmptv.shared_core.services.*

object ServiceLocator {

    private var catalogSource: CatalogSource? = null
    private var authProvider: AuthProvider? = null
    private var contentRepository: ContentRepository? = null
    private var sessionManager: SessionManager? = null
    private var applicationManager: TVApplicationManager? = null

    fun configure(
        catalogSource: CatalogSource = CatalogService(),
        authProvider: AuthProvider = AuthProvider { _, _ -> false },
    ) {
        this.catalogSource = catalogSource
        this.authProvider = authProvider
        this.contentRepository = null
        this.sessionManager = null
        this.applicationManager = null
    }

    fun contentRepository(): ContentRepository {
        return contentRepository ?: ContentRepositoryImpl(
            catalogSource ?: CatalogService()
        ).also { contentRepository = it }
    }

    fun sessionManager(): SessionManager {
        return sessionManager ?: SessionManagerImpl(
            authProvider ?: AuthProvider { _, _ -> false }
        ).also { sessionManager = it }
    }

    fun applicationManager(): TVApplicationManager {
        return applicationManager ?: TVApplicationManagerImpl().also { applicationManager = it }
    }

    fun reset() {
        catalogSource = null
        authProvider = null
        contentRepository = null
        sessionManager = null
        applicationManager = null
    }
}
