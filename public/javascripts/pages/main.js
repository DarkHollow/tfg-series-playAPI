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

// navegación !
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
    error: function(data) {
      console.log('error');
      var title = 'Conexión perdida';
      var text = 'Por alguna razón no se puede contactar con el servidor, prueba dentro de un rato.';
      if (data.status === 401) {
        title = 'Autorización denegada';
        text = 'No tienes autorización para esta acción';
      }
      swal({
        title: title,
        text: text,
        type: 'error',
        closeOnConfirm: true,
        confirmButtonText: 'Cerrar'
      });
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

// vista tvShow
// ver una serie
$(document).on('click', '[data-action=view-tvShow]', function() {
  var tvShowId = $(this).attr('data-id');
  var host = $(this).attr('data-host');
  if (host === undefined) {
    host = $('#host').html();
  }
  var route = 'http://' + host + '/admin/tvShows/' + tvShowId;
  console.log(route);
  getRoute(route);
  return false;
});

// vista tvShowRequests
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

// adminMain
// cargar evolutions
$(document).on('click', '[data-evolution=load]', function(e) {
  e.preventDefault();
  let button = $(this);
  let host = 'http://' + button.attr('data-host');
  let modal = $('#evolution_modal');
  let modalTitle = $('#upgrade-title');
  // obtener evolutions

  modal.block({
    message: '<div class="modal-full-rob"><i class="icon-spinner9 spinner"></i> Cargando datos...</div>',
    overlayCSS: {
      backgroundColor: '#fff',
      opacity: 0.8,
      cursor: 'wait'
    },
    css: {
      border: 0,
      padding: 0,
      backgroundColor: 'none'
    }
  });

  // pedir datos de evolutions
  var promises = [];
  promise = $.ajax({
    url: host + '/admin/evolutions',
    type: 'GET',
    dataType: 'json',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + window.localStorage.getItem('jwt')
    },
    success: function (response) {
      // comprobamos si hay version nueva
      if (response.newVersion !== undefined && response.newVersion !== 'none') {
        let actualVersion = response.actualVersion;
        let newVersion = response.newVersion;

        modalTitle.html('<i class="icon-server"></i> Actualización necesaria de v' + actualVersion + ' a v' + newVersion);

        var body = '<h6 class="text-semibold">Información</h6>' +
          '<p>Versión actual: <span id="actual-version">' + actualVersion + '</span></p>' +
          '<p>Versión nueva: <span id="new-version">'+ newVersion + '</span></p>' +
          '<hr />' +
          '<p>Es necesario aplicar esta actualización para el correcto funcionamiento del sistema debido a ' +
          'recientes actualizaciones. Cuando comience la actualización, espera a que finalice correctamente.</p>' +
          '<p>Aviso: si no aplicas esta actualización, el sistema no funcionará correctamente y podría dañar los ' +
          'datos existentes, te recomendamos que actualices antes de realizar ninguna acción.</p>';

        body += '<button type="button" class="btn btn-sm btn-success btn-labeled" data-host="' + button.attr('data-host') + '" data-evolution="upgrade">' +
          '<b><i class="icon-play4"></i></b>' +
          'Comenzar actualización' +
          '</button>';

        $('#upgrade-body').html(body);
      } else {
        // no hay actualizaciones que aplicar
        modalTitle.html('<i class="icon-server"></i> No hay actualizaciones disponibles');
        body = 'El sistema está actualizado a la última versión.';
        $('#upgrade-body').html(body);
      }

    },
    error: function () {
      modalTitle.html('<i class="icon-server"></i> Actualización');
      var body = 'Error obteniendo datos';
      $('#upgrade-body').html(body);
    }
  });
  promises.push(promise);

  $.when.apply(null, promises).done(function() {
    modal.unblock();
  }).fail(function() {
    modal.unblock();
  });

  return false;
});

// ejecutar evolution
$(document).on('click', '[data-evolution=upgrade]', function(e) {
  e.preventDefault();
  let button = $(this);
  let host = 'http://' + button.attr('data-host');
  let modal = $('#evolution_modal');


  // pedir datos de evolutions no aplicadas
  var promises = [];
  promise = $.ajax({
    url: host + '/admin/evolutions/notApplied',
    type: 'GET',
    dataType: 'json',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + window.localStorage.getItem('jwt')
    },
    success: function (response) {
      console.log(response);
      // comprobamos si hay evolution sin aplicar
      if (response.hasOwnProperty('evolutions') && response.evolutions !== null && response.evolutions.length > 0) {
        console.log('Evolutions a aplicar: ' + response.evolutions.length);

        // las recorremos para aplicarlas
        for (var i = 0; i < response.evolutions.length; i++) {
          var evolution = response.evolutions[i];
          if (evolution.hasOwnProperty('version') && evolution.version !== null) {
            console.log('Aplicando actualización versión ' + evolution.version);

          }
        }
      } else {
        // no hay evolutions que aplicar
        console.log('No hay evolutions que aplicar?');
      }
    },
    error: function () {
      console.log('Upgrade - Error');
    }
  });
  promises.push(promise);

  $.when.apply(null, promises).done(function() {
    modal.unblock();
  }).fail(function() {
    modal.unblock();
  });

  return false;
});
