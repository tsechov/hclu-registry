'use strict';

angular.module('smlHreg.docadd').controller('DocAddCtrl', function DocAddCtrl($q, $scope, $log, DocAddService, NotificationsService, FileUploader) {
    $scope.master = {};
    $scope.doc = {};
    $scope.remoteErrors = {};


    var self = this;
    var error = function (fileItem, response, status/*, headers */) {

        if (status === 400) {
            NotificationsService.showError("Something went wrong locally...");
        } else if(status === 413){
            NotificationsService.showError("The file is toooo large...  ");
        }else if (status === 500) {
            NotificationsService.showError("Something went wrong on the server...  " + response.id);
        } else {
            NotificationsService.showError("Something went wrong...");
        }
    };
    var uploaderBuilder = function (uploaderName, successHandler, errorHandler) {
        var uploader = new FileUploader({
            url: 'api/upload'
        });

        uploader.filters.push({
            name: 'customFilter',
            fn: function (/*item, options*/) {
                return this.queue.length < 1;
            }
        });

        uploader.removeAfterUpload = true;

        // CALLBACKS

        uploader.onWhenAddingFileFailed = function (item /*{File|FileLikeObject}*/, filter, options) {
            console.info(uploaderName + ': onWhenAddingFileFailed', item, filter, options);
        };
        uploader.onAfterAddingFile = function (fileItem) {
            console.info(uploaderName + ': onAfterAddingFile', fileItem);
        };
        uploader.onAfterAddingAll = function (addedFileItems) {
            console.info(uploaderName + ': onAfterAddingAll', addedFileItems);
        };
        uploader.onBeforeUploadItem = function (item) {
            console.info(uploaderName + ': onBeforeUploadItem', item);
        };
        uploader.onProgressItem = function (fileItem, progress) {
            console.info(uploaderName + ': onProgressItem', fileItem, progress);
        };
        uploader.onProgressAll = function (progress) {
            console.info(uploaderName + ': onProgressAll', progress);
        };
        uploader.onSuccessItem = function (fileItem, response, status, headers) {
            console.info(uploaderName + ': onSuccessItem', fileItem, response, status, headers);
            console.info(uploaderName + ': uploaded: ' + fileItem._file.name);
            successHandler(fileItem._file.name, response.id);
        };
        uploader.onErrorItem = function (fileItem, response, status, headers) {
            console.info(uploaderName + ': onErrorItem', fileItem, response, status, headers);

            errorHandler(fileItem, response, status, headers);

        };
        uploader.onCancelItem = function (fileItem, response, status, headers) {
            console.info(uploaderName + ': onCancelItem', fileItem, response, status, headers);
        };
        uploader.onCompleteItem = function (fileItem, response, status, headers) {
            console.info(uploaderName + ': onCompleteItem', fileItem, response, status, headers);

        };
        uploader.onCompleteAll = function () {
            console.info(uploaderName + ': onCompleteAll');
        };

        console.info(uploaderName + ': ', uploader);

        return uploader;
    };


    $scope.scannedUploader = uploaderBuilder("scannedUploader", function (name, id) {
        $scope.doc.scannedDocumentName = name;
        $scope.doc.scannedDocumentId = id;
        NotificationsService.showSuccess('Upload successful');
        $log.debug(!angular.isString($scope.doc.scannedDocumentName) || $scope.doc.scannedDocumentName.length === 0);
    }, error);

    $scope.emailUploader = uploaderBuilder("emailUploader", function (name, id) {
        $scope.doc.emailDocumentName = name;
        $scope.doc.emailDocumentId = id;
        NotificationsService.showSuccess('Upload successful');
    }, error);


    $scope.addDocument = function () {
        if ($scope.addForm.$valid) {
            DocAddService.addDocument($scope.doc).then(function (response) {
                NotificationsService.showSuccess('Document stored with registration number: ' + response.regId);
                self.reset();
            }, function (response) {
                if (response.status === 400 && angular.isArray(response.data.validationErrors)) {
                    angular.forEach(response.data.validationErrors, function (value) {
                        $scope.addForm[value.field].$setValidity("remote", false);
                        $scope.remoteErrors[value.field] = value.msg;
                    });
                } else {
                    NotificationsService.showError("Something went wrong...");
                    self.reset();
                }
            });
        }

    };

    $scope.removeDocument = function(docName){
        $scope.doc[docName+'DocumentName'] = undefined;
        $scope.doc[docName+'DocumentId'] = undefined;
    };

    this.reset = function () {
        $scope.doc = angular.copy($scope.master);
        $scope.remoteErrors = angular.copy($scope.master);
        $scope.addForm.$setPristine();
        $scope.addForm.$setUntouched();
    };


});