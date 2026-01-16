package kr.hs.jung.example.data.remote

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersistentCookieJar @Inject constructor() : CookieJar {
    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val host = url.host
        cookieStore.getOrPut(host) { mutableListOf() }.apply {
            cookies.forEach { newCookie ->
                removeAll { it.name == newCookie.name && it.path == newCookie.path }
                add(newCookie)
            }
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val host = url.host
        return cookieStore[host]?.filter { cookie ->
            !cookie.expiresAt.let { it < System.currentTimeMillis() }
        } ?: emptyList()
    }

    fun clear() {
        cookieStore.clear()
    }
}
