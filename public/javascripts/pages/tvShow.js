// tvShows

// volver a descargar datos de la serie
$(document).on('click', '[data-action=download-data]', function(e) {
  console.log('redescargar datos');
  e.preventDefault();
  let button = $(this);
  let buttonIcon = $("i", this);
  let tvShowId = button.attr('data-id');
  let host = 'http://' + button.attr('data-host');
  let redirect = button.attr('data-reload');
  let htmlDataOk = '<i class="icon-checkmark4 text-green"></i>';
  let htmlDataError = '<i class="icon-cross2 text-danger"></i>';

  $('#tvShow_panel').block({
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
      title: 'Descargar datos',
      text: '¿Seguro que quieres volver a descargar los datos? Solo se volverá a descargar los datos principales.',
      type: 'info',
      showCancelButton: true,
      closeOnConfirm: true,
      confirmButtonText: 'Aceptar',
      cancelButtonText: 'Cancelar'
    },
    function(isConfirm) {
      if (isConfirm) {
        // cambiar estado
        buttonIcon.attr('class', 'icon-spinner9 spinner');

        // hacer peticion
        var promises = [];
        let promise = $.ajax({
          url: host + '/api/tvshows/' + tvShowId,
          type: 'PUT',
          data: JSON.stringify({ "update": "data" }),
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + window.localStorage.getItem('jwt')
          },
          success: function (response) {
            if (response.ok !== null) {
              console.log('serie actualizada, recargando...');
              getRoute(redirect);
            } else {
              console.log("success - error persistiendo");
              swal({
                  title: 'Error',
                  text: 'No se ha podido descargar los datos de la serie en este momento',
                  type: 'error',
                  showCancelButton: false,
                  closeOnConfirm: true,
                  confirmButtonText: 'Aceptar'
                },
                function() {
                  buttonIcon.attr('class', 'icon-download');
                  $('#tvShow_panel').unblock();
                });
            }
          },
          error: function (response) {
            console.log("error - error persistiendo");
            setTimeout(function() {
              swal({
                  title: 'Error',
                  text: 'No se ha podido descargar los datos de la serie en este momento',
                  type: 'error',
                  showCancelButton: false,
                  closeOnConfirm: true,
                  confirmButtonText: 'Aceptar'
                },
                function() {
                  buttonIcon.attr('class', 'icon-download');
                  $('#tvShow_panel').unblock();
                });
            }, 500);
          }
        });
        promises.push(promise);

        $.when.apply(null, promises).done(function() {
          // redirigir...
        });
      } else {
        // se ha cancelado el popup
        $('#tvShow_panel').unblock();
      }

    });
});

// volver a descargar imágenes de la serie
$(document).on('click', '[data-action=download-images]', function(e) {
  console.log('redescargar imagenes');
  e.preventDefault();
  let button = $(this);
  let buttonIcon = $("i", this);
  let tvShowId = button.attr('data-id');
  let host = 'http://' + button.attr('data-host');
  let redirect = button.attr('data-reload');
  let htmlDataOk = '<i class="icon-checkmark4 text-green"></i>';
  let htmlDataError = '<i class="icon-cross2 text-danger"></i>';

  $('#tvShow_panel').block({
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
      title: 'Descargar imágenes',
      text: '¿Seguro que quieres volver a descargar los datos? Solo se volverá a descargar las imágenes.',
      type: 'info',
      showCancelButton: true,
      closeOnConfirm: true,
      confirmButtonText: 'Aceptar',
      cancelButtonText: 'Cancelar'
    },
    function(isConfirm) {
      if (isConfirm) {
        // cambiar estado
        buttonIcon.attr('class', 'icon-spinner9 spinner');

        // hacer peticion
        var promises = [];
        let promise = $.ajax({
          url: host + '/api/tvshows/' + tvShowId,
          type: 'PUT',
          data: JSON.stringify({ "update": "images" }),
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + window.localStorage.getItem('jwt')
          },
          success: function (response) {
            if (response.ok !== null) {
              console.log('imagenes de la serie actualizadas, recargando...');
              let htmlDataOk = '<i class="icon-checkmark4 text-green"></i>';
              let htmlDataError = '<i class="icon-cross2 text-danger"></i>';
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

              let text = '<ul style="width: 200px;" class="media-list check-list dropdown-content-body center-block">' +
                '<li><span class="pull-right">' + bannerData + '</span>Banner</li>' +
                '<li><span class="pull-right">' + posterData + '</span>Poster</li>' +
                '<li><span class="pull-right">' + fanartData + '</span>Fanart</li>' +
                '</ul>';
              swal({
                  title: 'Descargar imágenes',
                  text: text,
                  html: true,
                  type: 'success',
                  showCancelButton: false,
                  closeOnConfirm: true,
                  confirmButtonText: 'Aceptar'
                },
                function() {
                  getRoute(redirect);
                });
            } else {
              console.log("success - error actualizando imagenes");
              swal({
                  title: 'Error',
                  text: 'No se ha podido descargar las imágenes de la serie en este momento',
                  type: 'error',
                  showCancelButton: false,
                  closeOnConfirm: true,
                  confirmButtonText: 'Aceptar'
                },
                function() {
                  buttonIcon.attr('class', 'icon-icon-image2');
                  $('#tvShow_panel').unblock();
                });
            }
          },
          error: function (response) {
            console.log("error - error actualizando imagenes");
            setTimeout(function() {
              swal({
                  title: 'Error',
                  text: 'No se ha podido descargar las imágenes de la serie en este momento',
                  type: 'error',
                  showCancelButton: false,
                  closeOnConfirm: true,
                  confirmButtonText: 'Aceptar'
                },
                function() {
                  buttonIcon.attr('class', 'icon-icon-image2');
                  $('#tvShow_panel').unblock();
                });
            }, 500);
          }
        });
        promises.push(promise);

        $.when.apply(null, promises).done(function() {
          // redirigir...
        });
      } else {
        // se ha cancelado el popup
        $('#tvShow_panel').unblock();
      }

    });
});

// volver a descargar temporadas de la serie
$(document).on('click', '[data-action=download-seasons]', function(e) {
  console.log('redescargar temporadas');
  e.preventDefault();
  let button = $(this);
  let buttonIcon = $("i", this);
  let tvShowId = button.attr('data-id');
  let host = 'http://' + button.attr('data-host');
  let redirect = button.attr('data-reload');
  let htmlDataOk = '<i class="icon-checkmark4 text-green"></i>';
  let htmlDataError = '<i class="icon-cross2 text-danger"></i>';

  $('#tvShow_panel').block({
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
      title: 'Descargar temporadas',
      text: '¿Seguro que quieres volver a descargar las temporadas? Lo demás se quedará como está.\nEste proceso puede ' +
      'durar unos minutos dependiendo de la cantidad de temporadas y episodios de la serie.',
      type: 'info',
      showCancelButton: true,
      closeOnConfirm: true,
      confirmButtonText: 'Aceptar',
      cancelButtonText: 'Cancelar'
    },
    function(isConfirm) {
      if (isConfirm) {
        // cambiar estado
        buttonIcon.attr('class', 'icon-spinner9 spinner');

        // hacer peticion
        var promises = [];
        let promise = $.ajax({
          url: host + '/api/tvshows/' + tvShowId,
          type: 'PUT',
          data: JSON.stringify({ "update": "seasons" }),
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + window.localStorage.getItem('jwt')
          },
          success: function (response) {
            if (response.ok !== null) {
              console.log('serie actualizada, recargando...');
              getRoute(redirect);
            } else {
              console.log("success - error persistiendo");
              swal({
                  title: 'Error',
                  text: 'No se ha podido descargar los datos de la serie en este momento',
                  type: 'error',
                  showCancelButton: false,
                  closeOnConfirm: true,
                  confirmButtonText: 'Aceptar'
                },
                function() {
                  buttonIcon.attr('class', 'icon-download');
                  $('#tvShow_panel').unblock();
                });
            }
          },
          error: function (response) {
            console.log("error - error persistiendo");
            setTimeout(function() {
              swal({
                  title: 'Error',
                  text: 'No se ha podido descargar los datos de las temporadas en este momento',
                  type: 'error',
                  showCancelButton: false,
                  closeOnConfirm: true,
                  confirmButtonText: 'Aceptar'
                },
                function() {
                  buttonIcon.attr('class', 'icon-download');
                  $('#tvShow_panel').unblock();
                });
            }, 500);
          }
        });
        promises.push(promise);

        $.when.apply(null, promises).done(function() {
          // redirigir...
        });
      } else {
        // se ha cancelado el popup
        $('#tvShow_panel').unblock();
      }

    });
});

// volver a descargar los episodios de todas las temporadas de la serie
$(document).on('click', '[data-action=download-episodes]', function(e) {
  console.log('redescargar episodios');
  e.preventDefault();
  let button = $(this);
  let buttonIcon = $("i", this);
  let tvShowId = button.attr('data-id');
  let host = 'http://' + button.attr('data-host');
  let redirect = button.attr('data-reload');
  let htmlDataOk = '<i class="icon-checkmark4 text-green"></i>';
  let htmlDataError = '<i class="icon-cross2 text-danger"></i>';

  $('#tvShow_panel').block({
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
      title: 'Descargar episodios',
      text: '¿Seguro que quieres volver a descargar los episodios? Lo demás se quedará como está.\nEste proceso puede ' +
      'durar unos minutos dependiendo del número de episodios de la serie.',
      type: 'info',
      showCancelButton: true,
      closeOnConfirm: true,
      confirmButtonText: 'Aceptar',
      cancelButtonText: 'Cancelar'
    },
    function(isConfirm) {
      if (isConfirm) {
        // cambiar estado
        buttonIcon.attr('class', 'icon-spinner9 spinner');

        // hacer peticion
        var promises = [];
        let promise = $.ajax({
          url: host + '/api/tvshows/' + tvShowId,
          type: 'PUT',
          data: JSON.stringify({ "update": "episodes" }),
          headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + window.localStorage.getItem('jwt')
          },
          success: function (response) {
            if (response.ok !== null) {
              console.log('serie actualizada, recargando...');
              getRoute(redirect);
            } else {
              console.log("success - error persistiendo");
              swal({
                  title: 'Error',
                  text: 'No se ha podido descargar los datos de la serie en este momento',
                  type: 'error',
                  showCancelButton: false,
                  closeOnConfirm: true,
                  confirmButtonText: 'Aceptar'
                },
                function() {
                  buttonIcon.attr('class', 'icon-download');
                  $('#tvShow_panel').unblock();
                });
            }
          },
          error: function (response) {
            console.log("error - error persistiendo");
            setTimeout(function() {
              swal({
                  title: 'Error',
                  text: 'No se ha podido descargar los datos de los episodios en este momento',
                  type: 'error',
                  showCancelButton: false,
                  closeOnConfirm: true,
                  confirmButtonText: 'Aceptar'
                },
                function() {
                  buttonIcon.attr('class', 'icon-download');
                  $('#tvShow_panel').unblock();
                });
            }, 500);
          }
        });
        promises.push(promise);

        $.when.apply(null, promises).done(function() {
          // redirigir...
        });
      } else {
        // se ha cancelado el popup
        $('#tvShow_panel').unblock();
      }

    });
});

// eliminar una serie
$(document).on('click', '[data-action=delete-tvShow]', function(e) {
  console.log('eliminar serie');
  e.preventDefault();
  let button = $(this);
  let buttonIcon = $("i", this);
  let tvShowId = button.attr('data-id');
  let host = 'http://' + button.attr('data-host');
  let redirect = button.attr('data-redirect');

  $('#tvShow_panel').block({
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
      title: 'Eliminar serie',
      text: '¿Seguro que quieres eliminar esta serie? Esta acción no se podrá deshacer.',
      type: 'warning',
      showCancelButton: true,
      closeOnConfirm: true,
      confirmButtonText: 'Aceptar',
      cancelButtonText: 'Cancelar'
    },
    function(isConfirm) {
      if (isConfirm) {
        // cambiar estado
        buttonIcon.attr('class', 'icon-spinner9 spinner');

        // hacer peticion
        var promises = [];
        let promise = $.ajax({
          url: host + '/api/tvshows/' + tvShowId,
          type: 'DELETE',
          headers: {
            'Accept': 'application/json',
            'Authorization': 'Bearer ' + window.localStorage.getItem('jwt')
          },
          success: function (response) {
            if (response.ok !== null) {
              console.log('serie eliminada, volver a listado de series');
              setTimeout(function() {
                swal({
                    title: 'Eliminar serie',
                    text: 'Serie eliminada con éxito',
                    html: true,
                    type: 'success',
                    showCancelButton: false,
                    closeOnConfirm: true,
                    confirmButtonText: 'Aceptar'
                  },
                  function() {
                    getRoute(redirect);
                  });
              }, 500);
            } else {
              console.log("success - error eliminando serie");
              swal({
                  title: 'Error',
                  text: 'No se ha podido la serie en este momento',
                  type: 'error',
                  showCancelButton: false,
                  closeOnConfirm: true,
                  confirmButtonText: 'Aceptar'
                },
                function() {
                  buttonIcon.attr('class', 'icon-eraser');
                  $('#tvShow_panel').unblock();
                });
            }
          },
          error: function (response) {
            console.log("error - error eliminando serie");
            setTimeout(function() {
              swal({
                  title: 'Error',
                  text: 'No se ha podido la serie en este momento',
                  type: 'error',
                  showCancelButton: false,
                  closeOnConfirm: true,
                  confirmButtonText: 'Aceptar'
                },
                function() {
                  buttonIcon.attr('class', 'icon-eraser');
                  $('#tvShow_panel').unblock();
                });
            }, 500);
          }
        });
        promises.push(promise);

        $.when.apply(null, promises).done(function() {
          // redirigir...
        });
      } else {
        // se ha cancelado el popup
        $('#tvShow_panel').unblock();
      }

    });
});