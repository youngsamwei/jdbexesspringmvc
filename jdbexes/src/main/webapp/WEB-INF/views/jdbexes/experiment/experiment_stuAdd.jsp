<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/commons/global.jsp" %>
<script type="text/javascript">
    var  experiment_expstu_DataGrid;
    $(function() {
        experiment_expstu_DataGrid = $('#experiment_expstu_DataGrid').datagrid({
                        url : '${path }/dbexp/unSelectedDataGrid',
                        striped : true,
                        rownumbers : true,
                        pagination : true,
                        singleSelect : false,
                        idField : 'expno',
                        sortName : 'expno',
                        sortOrder : 'asc',
                        pageSize : 20,
                        frozenColumns : [ [{
                                field: 'expno',
                                checkbox: true,
                            },  {
                            width : '400',
                            title : '名称',
                            field : 'expname',
                            sortable : true
                        } ] ]
                    });
        $('#experiment_stuAddForm').form({
            url : '${path }/dbexperiment_stu/add',
            onSubmit : function() {
                progressLoad();

                var isValid = $(this).form('validate');
                if (!isValid) {
                    progressClose();
                }
                var rows = experiment_expstu_DataGrid.datagrid('getSelections');

                var expnos = [];
                if (rows && rows.length > 0) {
                    for ( var i = 0; i < rows.length; i++) {
                        expnos.push(rows[i].expno);
                    }
                }
                $('#expnos').val(expnos);
                return isValid;
            },
            success : function(result) {
                progressClose();
                result = $.parseJSON(result);
                if (result.success) {
                    parent.$.modalDialog.openner_dataGrid.datagrid('reload');//之所以能在这里调用到parent.$.modalDialog.openner_dataGrid这个对象，是因为user.jsp页面预定义好了
                    parent.$.modalDialog.handler.dialog('close');
                } else {
                    var form = $('#experiment_stuAddForm');
                    parent.$.messager.alert('错误', eval(result.msg), 'error');
                }
            }
        });

    });
</script>
<div class="easyui-layout" data-options="fit:true,border:false" >
    <div data-options="region:'center',border:false" style="overflow: hidden;padding: 3px;" >
        <table id="experiment_expstu_DataGrid" data-options="fit:true,border:false"></table>
        <form id="experiment_stuAddForm" method="post">
            <input name="expnos" id="expnos" type="hidden"  value="">
        </form>
    </div>
</div>