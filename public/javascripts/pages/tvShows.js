// tvShows

$(document).on('click', '[data-action=show-poster]', function(e) {
  console.log('mostrar poster');
  e.preventDefault();
  let tvShowId = $(this).attr('data-id');
  let imageUrl = 'http://' + $(this).attr('data-host') + $(this).attr('data-url').substring(1);
  $('#poster-' + tvShowId).html("<img class='tvShowListPoster img-responsive' src='" + imageUrl + "' />");
});

// mostrar datos de series eliminadas
$(document).on('click', '.panel [data-action=load-deleted-tvShows]', function(e) {
  e.preventDefault();
  deletedTvShowsTableClear();
  deletedTvShowsTableLoad();
  closeAdv('deleted-adv');
});

// cargar datos de una serie eliminada (boton descargar datos)
$(document).on('click', '[data-action=get-deleted-tvShow-data]', function(e) {
  console.log('cargar datos de serie eliminada');
  e.preventDefault();
  let button = $(this);
  let requestId = button.attr('data-id');
  let tvdbId = button.attr('data-tvdbid');
  let host = 'http://' + button.attr('data-host');
  button.block({
    message: '',
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
  $('i', button).attr('class', 'icon-spinner9 spinner');

  // pedir datos serie a tvdb
  var promises = [];
  promise = $.ajax({
    url: host + '/api/tvshows/tvdb/' + tvdbId,
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
    button.unblock();
    button.remove();
  }).fail(function() {
    button.unblock();
    button.remove();
  });

});

// cargar datos de una serie eliminada (boton descargar datos)
$(document).on('click', '[data-action=reaccept-tvShow]', function(e) {
  console.log('reaprobar serie eliminada');
  e.preventDefault();
  let button = $(this);
  let requestId = button.attr('data-id');
  let host = 'http://' + button.attr('data-host');
  button.block({
    message: '',
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
  $('i', button).attr('class', 'icon-spinner9 spinner');

  // petición de aceptar serie
  let htmlDataOk = '<i class="icon-checkmark4 text-green"></i>';
  let htmlDataError = '<i class="icon-cross2 text-danger"></i>';

  $('#deleted_tvShows_table_panel').block({
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
      title: 'Reaprobar petición',
      text: '¿Deseas volver a persistir esta serie?',
      type: 'info',
      showCancelButton: true,
      closeOnConfirm: true,
      confirmButtonText: 'Aceptar',
      cancelButtonText: 'Cancelar'
    },
    function(isConfirm) {
      if (isConfirm) {
        // desbloquear tabla
        $('#deleted_tvShows_table_panel').unblock();

        // hacer peticion
        var promises2 = [];
        let promise2 = $.ajax({
          url: host + '/api/requests/' + requestId,
          type: 'PUT',
          headers: {
            'Accept': 'application/json',
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
                '<li><button type="button" data-id=' + response.tvShow.id + ' data-action="view-tvShow" data-host=' + $('#host').html() + ' class="btn btn-labeled btn-xs bg-primary btn-group-justified"><b><i class="icon-tv"></i></b> Ver serie</button></li>' +
                '</ul>' +
                '</li>' +
                '</ul>';
              $('#actions-' + requestId).html(status);
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
              $('#actions-' + requestId).html(status);
              $('#deleted_tvShows_table_panel').unblock();
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
            $('#actions-' + requestId).html(status);
            $('#deleted_tvShows_table_panel').unblock();
          }
        });
        promises2.push(promise2);

        $.when.apply(null, promises2).done(function() {
          $('#deleted_tvShows_table_panel').unblock();
        });
      } else {
        // se ha cancelado el popup
        button.unblock();
        $('i', button).attr('class', 'icon-check');
        $('#deleted_tvShows_table_panel').unblock();
      }

    });

});

// close advert
// cargar datos de una serie eliminada (boton descargar datos)
$(document).on('click', '[data-action=close-adv]', function(e) {
  e.preventDefault();
  var elementId = $(this).parent().attr('id');
  closeAdv(elementId);
});

function closeAdv(id) {
  var $advClose = $('#' + id);

  containerHeight(); // recalculate page height

  $advClose.slideUp(150, function() {
    $(this).remove();
  });
}

// Calculate min height
function containerHeight() {
  var availableHeight = $(window).height() - $('.page-container').offset().top - $('.navbar-fixed-bottom').outerHeight();

  $('.page-container').attr('style', 'min-height:' + availableHeight + 'px');
}

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
