package models.service;

import com.google.inject.Inject;
import models.Usuario;
import models.dao.UsuarioDAO;

public class UsuarioService {

  private final UsuarioDAO usuarioDAO;

  @Inject
  public UsuarioService(UsuarioDAO usuarioDAO) {
    this.usuarioDAO = usuarioDAO;
  }

  // Read de busqueda
  // buscar por id
  public Usuario find(Integer id) {
    return usuarioDAO.find(id);
  }

}
