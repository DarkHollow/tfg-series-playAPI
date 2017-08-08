// tvShows

// descargar datos de tvdb
$(document).on('click', '.panel [data-action=get-tvdb-data]', function(e) {
  e.preventDefault();
  requestsTableClear();
  requestsTableLoad();
});

// mostrar datos peticiones aceptadas
$(document).on('click', '.panel [data-action=load-persisted-requests]', function(e) {
  e.preventDefault();
  persistedRequestsTableClear();
  persistedRequestsTableLoad();
});

// mostrar datos peticiones rechazadas
$(document).on('click', '.panel [data-action=load-rejected-requests]', function(e) {
  e.preventDefault();
  rejectedRequestsTableClear();
  rejectedRequestsTableLoad();
});

// aceptar serie
$(document).on('click', '[data-action=accept-tvShow]', function(e) {
  console.log('aceptar');
  e.preventDefault();
  let requestId = $(this).attr('href');
  let htmlDataOk = '<i class="icon-checkmark4 text-green"></i>';
  let htmlDataError = '<i class="icon-cross2 text-danger"></i>';

  $('#requests_table_panel').block({
    message: '<i class="icon-spinner9 spinner"></i>',
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

  swal({
      title: 'Aceptar petición',
      text: '¿Deseas persistir esta serie?',
      type: 'info',
      showCancelButton: true,
      closeOnConfirm: true,
      confirmButtonText: 'Aceptar',
      cancelButtonText: 'Cancelar'
    },
    function(isConfirm) {
      if (isConfirm) {
        // cambiar estado
        $('#status-' + requestId).html('<i class="icon-spinner9 spinner"></i>');
        // desbloquear tabla
        $('#requests_table_panel').unblock();

        // hacer peticion
        var promises2 = [];
        var data = JSON.stringify({"requestId": requestId});
        let promise2 = $.ajax({
          url: 'http://localhost:9000/admin/tvshows/requests',
          type: 'PUT',
          data: data,
          dataType: 'json',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + window.localStorage.getItem('jwt')
          },
          success: function (response) {
            if (response.ok !== null) {
              let bannerData, posterData, fanartData;
              bannerData = posterData = fanartData = htmlDataOk;
              if (!response.banner) {
                bannerData = htmlDataError;
              }
              if (!response.poster) {
                posterData = htmlDataError;
              }
              if (!response.fanart) {
                fanartData = htmlDataError;
              }

              // cambiar estado
              let status = '<ul class="icons-list">' +
                '<li class="dropdown">' +
                '<a href="#" class="dropdown-toggle tooltips" data-toggle="dropdown" data-popup="tooltip" title="Resumen de persistencia">' +
                htmlDataOk +
                '</a>' +
                '<div class="dropdown-menu dropdown-content">' +
                '<div class="dropdown-content-heading">' +
                'Resumen' +
                '</div>' +
                '<ul class="media-list check-list dropdown-content-body">' +
                '<li><span class="pull-right">' + htmlDataOk + '</span>Datos de la serie</li>' +
                '<li><span class="pull-right">' + bannerData + '</span>Banner</li>' +
                '<li><span class="pull-right">' + posterData + '</span>Poster</li>' +
                '<li><span class="pull-right">' + fanartData + '</span>Fanart</li>' +
                '<li class="divider"></li>' +
                '<li><button type="button" class="btn btn-labeled btn-xs bg-primary btn-group-justified"><b><i class="icon-tv"></i></b> Ver serie</button></li>' +
                '</ul>' +
                '</li>' +
                '</ul>';
              $('#status-' + requestId).html(status);
            } else {
              console.log("ok - error persistiendo");
              // cambiar estado
              let status = '<ul class="icons-list">' +
                '<li class="dropdown">' +
                '<a href="#" class="dropdown-toggle tooltips" data-toggle="dropdown" data-popup="tooltip" title="Resumen de persistencia">' +
                htmlDataError +
                '</a>' +
                '<div class="dropdown-menu dropdown-content">' +
                '<div class="dropdown-content-heading">' +
                'Error persistiendo' +
                '</div>' +
                '<ul class="media-list check-list dropdown-content-body">' +
                '<li>' + response.responseJSON.error + '</li>' +
                '<li class="divider"></li>' +
                '<li><button type="button" data-action="get-list" class="btn btn-labeled btn-xs bg-warning btn-group-justified"><b><i class="icon-database-refresh"></i></b> Recargar lista</button></li>' +
                '</ul>' +
                '</li>' +
                '</ul>';
              $('#status-' + requestId).html(status);
              $('#requests_table_panel').unblock();
            }
          },
          error: function (response) {
            console.log("error - error persistiendo");
            // cambiar estado
            let status = '<ul class="icons-list">' +
              '<li class="dropdown">' +
              '<a href="#" class="dropdown-toggle tooltips" data-toggle="dropdown" data-popup="tooltip" title="Resumen de persistencia">' +
              htmlDataError +
              '</a>' +
              '<div class="dropdown-menu dropdown-content">' +
              '<div class="dropdown-content-heading">' +
              'Error persistiendo' +
              '</div>' +
              '<ul class="media-list check-list dropdown-content-body">' +
              '<li>' + response.responseJSON.error + '</li>' +
              '<li class="divider"></li>' +
              '<li><button type="button" data-action="get-list" class="btn btn-labeled btn-xs bg-warning btn-group-justified"><b><i class="icon-database-refresh"></i></b> Recargar lista</button></li>' +
              '</ul>' +
              '</li>' +
              '</ul>';
            $('#status-' + requestId).html(status);
            $('#requests_table_panel').unblock();
          }
        });
        promises2.push(promise2);

        $.when.apply(null, promises2).done(function() {
          $('#requests_table_panel').unblock();
        });
      } else {
        // se ha cancelado el popup
        $('#requests_table_panel').unblock();
      }

    });
});

// rechazar serie
$(document).on('click', '[data-action=reject-tvShow]', function(e) {
  console.log('rechazar');
  e.preventDefault();
  let requestId = $(this).attr('href');
  let htmlDataOk = '<i class="icon-checkmark4 text-green"></i>';
  let htmlDataError = '<i class="icon-cross2 text-danger"></i>';

  $('#requests_table_panel').block({
    message: '<i class="icon-spinner9 spinner"></i>',
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

  swal({
      title: 'Rechazar petición',
      text: '¿Deseas rechazar esta serie?',
      type: 'info',
      showCancelButton: true,
      closeOnConfirm: true,
      confirmButtonText: 'Aceptar',
      cancelButtonText: 'Cancelar'
    },
    function(isConfirm) {
      if (isConfirm) {
        // cambiar estado
        $('#status-' + requestId).html('<i class="icon-spinner9 spinner"></i>');
        // desbloquear tabla
        $('#requests_table_panel').unblock();

        // hacer peticion
        var promises2 = [];
        var data = JSON.stringify({"requestId": requestId});
        let promise2 = $.ajax({
          url: 'http://localhost:9000/admin/tvshows/requests',
          type: 'PATCH',
          data: data,
          dataType: 'json',
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + window.localStorage.getItem('jwt')
          },
          success: function (response) {
            if (response.ok !== null) {

              // cambiar estado
              let status = '<ul class="icons-list">' +
                '<li class="dropdown">' +
                '<a href="#" class="dropdown-toggle tooltips" data-toggle="dropdown" data-popup="tooltip" title="Resumen de rechazo">' +
                htmlDataOk +
                '</a>' +
                '<div class="dropdown-menu dropdown-content">' +
                '<div class="dropdown-content-heading">' +
                'Resumen' +
                '</div>' +
                '<ul class="media-list check-list dropdown-content-body">' +
                '<li>Petición rechazada</li>' +
                '<li class="divider"></li>' +
                '<li><button type="button" class="btn btn-labeled btn-xs bg-danger btn-group-justified"><b><i class="icon-tv"></i></b> Ver rechazos</button></li>' +
                '</ul>' +
                '</li>' +
                '</ul>';
              $('#status-' + requestId).html(status);
            } else {
              console.log("ok - error rechazando");
              // cambiar estado
              let status = '<ul class="icons-list">' +
                '<li class="dropdown">' +
                '<a href="#" class="dropdown-toggle tooltips" data-toggle="dropdown" data-popup="tooltip" title="Resumen de rechazo">' +
                htmlDataError +
                '</a>' +
                '<div class="dropdown-menu dropdown-content">' +
                '<div class="dropdown-content-heading">' +
                'Error rechazando petición' +
                '</div>' +
                '<ul class="media-list check-list dropdown-content-body">' +
                '<li>' + response.responseJSON.error + '</li>' +
                '<li class="divider"></li>' +
                '<li><button type="button" data-action="get-list" class="btn btn-labeled btn-xs bg-warning btn-group-justified"><b><i class="icon-database-refresh"></i></b> Recargar lista</button></li>' +
                '</ul>' +
                '</li>' +
                '</ul>';
              $('#status-' + requestId).html(status);
              $('#requests_table_panel').unblock();
            }
          },
          error: function (response) {
            console.log("error - error rechazando");
            // cambiar estado
            let status = '<ul class="icons-list">' +
              '<li class="dropdown">' +
              '<a href="#" class="dropdown-toggle tooltips" data-toggle="dropdown" data-popup="tooltip" title="Resumen de rechazo">' +
              htmlDataError +
              '</a>' +
              '<div class="dropdown-menu dropdown-content">' +
              '<div class="dropdown-content-heading">' +
              'Error rechanzando petición' +
              '</div>' +
              '<ul class="media-list check-list dropdown-content-body">' +
              '<li>' + response.responseJSON.error + '</li>' +
              '<li class="divider"></li>' +
              '<li><button type="button" data-action="get-list" class="btn btn-labeled btn-xs bg-warning btn-group-justified"><b><i class="icon-database-refresh"></i></b> Recargar lista</button></li>' +
              '</ul>' +
              '</li>' +
              '</ul>';
            $('#status-' + requestId).html(status);
            $('#requests_table_panel').unblock();
          }
        });
        promises2.push(promise2);

        $.when.apply(null, promises2).done(function() {
          $('#requests_table_panel').unblock();
        });
      } else {
        // se ha cancelado el popup
        $('#requests_table_panel').unblock();
      }

    });
});

// cargar datos de una serie rechazada (boton descargar datos)
$(document).on('click', '[data-action=get-tvShow-data]', function(e) {
  console.log('cargar datos de serie rechazada');
  e.preventDefault();
  let requestId = $(this).attr('data-id');
  let tvdbId = $(this).attr('data-tvdbid');
  $('#actions-' + requestId).html('<i class="icon-spinner9 spinner"></i>');

  // pedir datos serie a tvdb
  var promises = [];
  promise = $.ajax({
    url: 'http://localhost:9000/api/tvshows/tvdb/' + tvdbId,
    type: 'GET',
    dataType: 'json',
    headers: {
      'Accept': 'application/json',
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + window.localStorage.getItem('jwt')
    },
    success: function (response) {
      let name = response.name;
      let firstAired = response.firstAired;
      let banner;

      if (response.banner !== "") {
        banner = '<img class="img-responsive center-block" style="max-height: 65px;" src="http://thetvdb.com/banners/' + response.banner + '" />';
      }

      // poner datos
      $('#name-' + requestId).html(name);
      $('#banner-' + requestId).html(banner);
      $('#firstAired-' + requestId).html(firstAired);
    },
    error: function (response) {

    }
  });
  promises.push(promise);

  $.when.apply(null, promises).done(function() {
    $('#actions-' + requestId).html('<i class="icon-checkmark4 text-green"></i>');
  }).fail(function() {
    $('#actions-' + requestId).html('<i class="icon-cross2 text-danger"></i>');
  });

});

$(function() {

  // Table setup
  // ------------------------------

  // Setting datatable defaults
  $.extend( $.fn.dataTable.defaults, {
    autoWidth: false,
    dom: '<"datatable-header"fl><"datatable-scroll"t><"datatable-footer"ip>',
    language: {
      search: '<span>Filtrar:</span> _INPUT_',
      searchPlaceholder: 'Escribe para filtrar...',
      lengthMenu: '<span>Mostrar:</span> _MENU_',
      paginate: { 'first': 'Primera', 'last': 'Última', 'next': '&rarr;', 'previous': '&larr;' }
    },
    drawCallback: function () {
      $(this).find('tbody tr').slice(-3).find('.dropdown, .btn-group').addClass('dropup');
    },
    preDrawCallback: function() {
      $(this).find('tbody tr').slice(-3).find('.dropdown, .btn-group').removeClass('dropup');
    }
  });

  // External table additions
  // ------------------------------

  // Enable Select2 select for the length option
  $('.dataTables_length select').select2({
    minimumResultsForSearch: Infinity,
    width: 'auto'
  });

});
