$(function () {
    var policyInstance = new Policy();

    var promised_get_tiers =  policyInstance.getAllPoliciesByTier("subscription");

    promised_get_tiers.then(function (response) {
        var raw_data = {
            data: response.obj
        };

        var callbacks = {
            onSuccess: function () {
                _initDataTable(raw_data);
            },
            onFailure: function (message, e) {

            }
        };
        var mode = "OVERWRITE";
        var obj = {};
        obj.list=response.obj;
        UUFClient.renderFragment("org.wso2.carbon.apimgt.web.admin.feature.policy-view", obj,
                                 "policy-view", mode, callbacks);
    });
    promised_get_tiers.catch(
            function (error) {
                console.log("Error occurred while loading swagger definition");
                if (error.status == 401) {
                    redirectToLogin(contextPath);
                }
            }
    );

    function _initDataTable(raw_data) {
        $('#api-policy').DataTable({
            ajax: function (data, callback, settings) {
                callback(raw_data);
            },
            columns: [
                {'data': 'name'},
                {'data': 'description'},
                {'data': 'unitTime'},
                {'data': 'timeUnit'},
                {'data': 'name'}
            ],
            columnDefs: [
                {
                    targets: ["policy-listing-action"], //class name will be matched on the TH for the column
                    searchable: false,
                    sortable: false,
                    render: _renderActionButtons // Method to render the action buttons per row
                }
            ]
        })
    }

    function _renderActionButtons(data, type, row) {
        if (type === "display") {

            var editIcon1 = $("<i>").addClass("fw fw-ring fw-stack-2x");
            var editIcon2 = $("<i>").addClass("fw fw-edit fw-stack-1x");
            var editSpanIcon = $("<span>").addClass("fw-stack").append(editIcon1).append(editIcon2);
            var editSpanText = $("<span>").addClass("hidden-xs").text("Edit");
            var edit_button = $('<a>', {
                id: data.id,
                href: contextPath + '/applications/' + data + '/edit',
                title: 'Edit'
            })
                    .addClass("btn  btn-sm padding-reduce-on-grid-view")
                    .append(editSpanIcon)
                    .append(editSpanText);

            var deleteIcon1 = $("<i>").addClass("fw fw-rin  g fw-stack-2x");
            var deleteIcon2 = $("<i>").addClass("fw fw-delete fw-stack-1x");
            var deleteSpanIcon = $("<span>").addClass("fw-stack").append(deleteIcon1).append(deleteIcon2);
            var deleteSpanText = $("<span>").addClass("hidden-xs").text("delete");
            var delete_button = $('<a>', {id: data, href: '#', 'data-id': data, title: 'delete'})
                    .addClass("btn btn-sm padding-reduce-on-grid-view deleteApp")
                    .append(deleteSpanIcon)
                    .append(deleteSpanText);
            return $('<div></div>').append(edit_button).append(delete_button).html();
        } else {
            return data;
        }
    }

})