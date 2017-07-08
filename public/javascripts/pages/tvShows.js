// tvShows

// actualizar tabla
$(document).on('click', '.panel [data-action=reload]', function() {
  requestsTableClear();
  requestsTableLoad();
});

$(function() {

  // Table setup
  // ------------------------------

  // Setting datatable defaults
  $.extend( $.fn.dataTable.defaults, {
    autoWidth: false,
    columnDefs: [{
      orderable: false,
      width: '100px',
      targets: [ 4 ]
    }],
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


  // Basic datatable
  $('.datatable-basic').DataTable();


  // Alternative pagination
  $('.datatable-pagination').DataTable({
    pagingType: "simple",
    language: {
      paginate: {'next': 'Siguiente &rarr;', 'previous': '&larr; Anterior'}
    }
  });


  // Datatable with saving state
  $('.datatable-save-state').DataTable({
    stateSave: true
  });


  // Scrollable datatable
  $('.datatable-scroll-y').DataTable({
    autoWidth: true,
    scrollY: 300
  });



  // External table additions
  // ------------------------------

  // Enable Select2 select for the length option
  $('.dataTables_length select').select2({
    minimumResultsForSearch: Infinity,
    width: 'auto'
  });

});
