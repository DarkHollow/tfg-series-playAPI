// tvShows

$(document).on('click', '[data-action=show-poster]', function(e) {
  console.log('mostrar poster');
  e.preventDefault();
  let tvShowId = $(this).attr('data-id');
  let imageUrl = 'http://' + $(this).attr('data-host') + $(this).attr('data-url').substring(1);
  $('#poster-' + tvShowId).html("<img class='tvShowListPoster img-responsive' src='" + imageUrl + "' />");
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
