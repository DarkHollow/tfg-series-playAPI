// main javascript

/* handlers */

// clicks de navegacion
$(document).on('click', '.ajaxLink', function() {
  var route = $(this).attr('href');
  getRoute(route);
  return false;
});

// click en logout
$(document).on('click', '.logout', function() {
  console.log("logout");
  logoutAdmin();
  window.location.href = '/admin/login';
  return false;
});

// botones de atras y adelante
window.onpopstate = function(e) {
  console.log(e);
  if (e.state) {
    document.getElementById("content").innerHTML = e.state.html;
    document.title = $('<div/>').append(e.state.html).find('title').text();
    showUserName();
  }
};

/* funciones */

// mostrar nombre usuario
function showUserName() {
  $('.userName').text(window.localStorage.getItem('userName'));
  $('.helloUserName').text('Hola, ' + window.localStorage.getItem('userName'));
}

function getRoute(route) {
  $.ajax({
    url: route,
    type: 'GET',
    headers: {
      'Authorization': 'Bearer ' + window.localStorage.getItem('jwt')
    },
    dataType: 'html',
    success: function(data) {
      var parsed = $('<div/>').append(data);
      $('#content').html(parsed.find('#content'));
      document.title = parsed.find('title').text();
      window.history.pushState( {'html': data, 'pageTitle': parsed.find('title').text()}, '', route );
    },
    error: function() {
      console.log('error');
    },
    complete: function() {
      showUserName();
    }
  });
}

// borrar jwt
function logoutAdmin() {
  window.localStorage.clear();
}

// tvShows
// actualizar lista de peticiones
$(document).on('click', '[data-action=get-list]', function() {
  //requestsTableClear();
  //requestsTableLoad();
  // ^ no sirve con borrar tabla y cargarla, necesitamos obtener de nuevo la lista de peticiones
  // asi que redireccionamos
  var route = $(this).attr('href');

  // si no tenemos ruta porque somos un boton en un archivo js lo obtengo de uno de la plantilla
  if (route === undefined || route === null) {
    route = $(".panel [data-action='get-list']").attr('href');
  }
  getRoute(route);
  return false;
});