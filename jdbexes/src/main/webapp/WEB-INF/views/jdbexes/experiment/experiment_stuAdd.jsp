<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/commons/global.jsp" %>

<script type="text/javascript">
    const experimentExpStu = function () {
        let datagrid = $('#experiment-stu-add-datagrid').datagrid({
            url: '${path}/dbexp/unSelectedDataGrid',
            striped: true,
            rownumbers: true,
            pagination: true,
            singleSelect: false,
            idField: 'expno',
            sortName: 'expno',
            sortOrder: 'asc',
            pageSize: 20,
            frozenColumns: [[{
                field: 'expno',
                checkbox: true,
            }, {
                width: '400',
                title: '名称',
                field: 'expname',
                sortable: true
            }]]
        });

        let form = $('#experiment-stu-add-form').form({
            url: '${ path }/dbexperiment_stu/add',
            onSubmit: onSubmit,
            success: onSuccess
        });

        function onSubmit() {
            progressLoad();

            const isValid = $(this).form('validate');
            if (!isValid) {
                progressClose();
            }
            const rows = datagrid.datagrid('getSelections');

            const expnos = [];
            if (rows && rows.length > 0) {
                for (let i = 0; i < rows.length; i++) {
                    expnos.push(rows[i].expno);
                }
            }
            $('#expnos').val(expnos);
            return isValid;
        }

        function onSuccess(result) {
            progressClose();
            result = $.parseJSON(result);
            if (result.success) {
                parent.$.modalDialog.openner_dataGrid.datagrid('reload'); // 能在这里调用到 openner_dataGrid 这个对象，是因为 user.jsp 页面预定义好了
                parent.$.modalDialog.handler.dialog('close');
            } else {
                parent.$.messager.alert('错误', eval(result.msg), 'error');
            }
        }

        return {
            datagrid: datagrid,
            addForm: form
        };
    }();

    window.experimentExpStu = experimentExpStu;
</script>

<style>
    .experiment-stu-add-container {
        overflow: hidden;
        padding: 3px;
    }
</style>

<div class="easyui-layout" data-options="fit:true,border:false">
    <div data-options="region:'center',border:false" class="experiment-stu-add-container">
        <table id="experiment-stu-add-datagrid" data-options="fit:true,border:false"></table>
        <form id="experiment-stu-add-form" method="post">
            <input name="expnos" id="expnos" type="hidden" value="">
        </form>
    </div>
</div>