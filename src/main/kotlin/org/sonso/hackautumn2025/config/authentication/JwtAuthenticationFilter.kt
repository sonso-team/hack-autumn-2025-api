package org.sonso.hackautumn2025.config.authentication

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.sonso.hackautumn2025.service.JwtService

@Component
class JwtAuthenticationFilter(
    private val userDetailsService: UserDetailsService,
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            if (
                request.requestURI.startsWith("/api/auth")
                && request.requestURI != "/api/auth/user-info"
                && request.requestURI != "/api/auth/admin/user-info"
            ) {
                filterChain.doFilter(request, response)
                return
            }

            val authHeader = request.getHeader("Authorization")

            if ((authHeader == null || authHeader.isEmpty()) || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response)
                return
            }

            val token = authHeader.substring(7)
            val userEmail = jwtService.getUsername(token)

            if (userEmail.isNotEmpty() && SecurityContextHolder.getContext().authentication == null) {
                val userDetails = userDetailsService.loadUserByUsername(userEmail)

                if (jwtService.isTokenValid(token, userDetails)) {
                    val authToken = UsernamePasswordAuthenticationToken(userDetails, token, userDetails.authorities)
                    authToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authToken
                } else return
            }
            filterChain.doFilter(request, response)
        } catch (ex: Exception) {
            logger.warn("Token validation ended with exception: ${ex.message}")
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            response.contentType = "application/json"
            response.writer.write(
                """
            {
                "status": 401,
                "error": "Unauthorized",
                "message": "Токен не валиден",
                "path": "${request.requestURI}"
            }
                """.trimIndent()
            )
        }
    }
}
