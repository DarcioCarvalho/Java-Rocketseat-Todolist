package br.com.darciocarvalho.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.darciocarvalho.todolist.user.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    var servletPath = request.getServletPath();

    if (servletPath.startsWith("/tasks/")) {
      // Pegar autenticação (usuário e senha)
      var authorization = request.getHeader("Authorization");
      var authEncoded = authorization.substring("Basic".length()).trim();

      System.out.println("Basic Auth: " + authEncoded);

      byte[] authDecode = Base64.getDecoder().decode(authEncoded);

      var authString = new String(authDecode);
      System.out.println("Authorization");
      System.out.println(authString);

      String[] credentials = authString.split(":");
      String username = credentials[0];
      String password = credentials[1];
      System.out.println(username);
      System.out.println(password);

      var userFound = this.userRepository.findByUsername(username);

      if (userFound == null) {
        response.sendError(401);
      } else {
        // Validar usuário
        var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), userFound.getPassword());

        if (!passwordVerify.verified) {
          response.sendError(401, password);
        }

        /*
         * if (passwordVerify.verified) {
         * filterChain.doFilter(request, response);
         * } else {
         * response.sendError(401, password);
         * }
         */

        request.setAttribute("idUser", userFound.getId());
      }

    }
    filterChain.doFilter(request, response);
  }

}
