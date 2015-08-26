angular.module('smlHreg.docadd').factory('DocAddService', function ($resource) {
    var docAddService = {};

    this.docAddResource = $resource('api/docs/add', {}, {
        'add': {method: 'POST'}
    }, {});



    var self = this;

    docAddService.addDocument = function (doc) {
        return self.docAddResource.add(doc).$promise;

    };

    return docAddService;
});