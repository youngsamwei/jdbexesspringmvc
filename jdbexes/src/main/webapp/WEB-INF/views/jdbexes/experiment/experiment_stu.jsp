<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/commons/global.jsp" %>
<script type="text/javascript">
    var  experiment_stuDataGrid;
    $(function() {
         experiment_stuDataGrid = $('#experiment_stuDataGrid').datagrid({
            url : '${path }/dbexperiment_stu/dataGrid',
            striped : true,
            rownumbers : true,
            pagination : true,
            singleSelect : true,
            idField : 'expstuno',
            sortName : 'expno',
            sortOrder : 'asc',
            pageSize : 20,
            pageList : [ 10, 20, 30, 40, 50, 100, 200, 300, 400, 500 ],
            frozenColumns : [ [ {
                width : '400',
                title : '实验名称',
                field : 'expname',
                sortable : true
            } ,  {
                 width : '200',
                 title : '测试描述',
                 field : 'testdesc',
                 sortable : true,
                 formatter : function(value, row, index){
                    var str=  "";
                    if (row.teststatus >= 2){
                        str = $.formatString("<a href='javascript:void(0)' onclick='open_test_log_experiment_stu_fun({0});' >{1}</a>", row.expstuno, value);
                        return str;
                    }else{
                        return value;
                    }
                 }
             } , {
                field : 'teststatus',
                title : '操作',
                width : 200,
                formatter : function(value, row, index) {
                    var str = '';
                    if (value == 0){
                        str = "正在测试..."
                    } else {

                        <shiro:hasPermission name="/dbexperiment_stu/edit">
                            str += '&nbsp;&nbsp;&nbsp;&nbsp;';
                            str += $.formatString('<a href="javascript:void(0)" class="experiment_stu-easyui-linkbutton-edit" data-options="plain:true,iconCls:\'fi-pencil icon-blue\'" onclick="editexperiment_stuFun(\'{0}\');" >提交文件</a>', row.expstuno);
                        </shiro:hasPermission>
                    }
                    return str;
                }
            } ] ],
            onLoadSuccess:function(data){
                $('.experiment_stu-easyui-linkbutton-edit').linkbutton({text:'提交文件'});
                $('.experiment_stu-easyui-linkbutton-del').linkbutton({text:'不选'});
            },
            toolbar : '#experiment_stuToolbar'
        });
    });

    function open_test_log_experiment_stu_fun(expstuno){
        parent.$.modalDialog({
                    title : '查看测试日志',
                    width : 700,
                    height : 600,
                    href : '${path }/dbexperiment_stu/openTestLogPage?expstuno=' + expstuno,
                    buttons : [ {
                        text : '关闭',
                        handler : function() {

                        }
                    } ]
                });
    }

    function addexperiment_stuFun() {
        parent.$.modalDialog({
            title : '选择实验',
            width : 700,
            height : 600,
            href : '${path }/dbexperiment_stu/addPage',
            buttons : [ {
                text : '确定',
                handler : function() {
                    parent.$.modalDialog.openner_dataGrid = experiment_stuDataGrid;//因为添加成功之后，需要刷新这个treeGrid，所以先预定义好
                    var f = parent.$.modalDialog.handler.find('#experiment_stuAddForm');
                    f.submit();
                }
            } ]
        });
    }

    function editexperiment_stuFun(id) {
        if (id == undefined) {
            var rows = experiment_stuDataGrid.datagrid('getSelections');
            id = rows[0].expstuno;
        } else {
            experiment_stuDataGrid.datagrid('unselectAll').datagrid('uncheckAll');
        }
        parent.$.modalDialog({
            title : '上传文件',
            width : 800,
            height : 500,
            href : '${path }/dbexperiment_stu/submitFilePage?expstuno=' + id,
            buttons : [ {
                text : '上传文件并开始测试',
                handler : function() {
                    parent.$.modalDialog.openner_dataGrid = experiment_stuDataGrid;//因为添加成功之后，需要刷新这个dataGrid，所以先预定义好
                    var f = parent.$.modalDialog.handler.find('#experiment_stuSubmitForm');
                    f.submit();
                }
            } ]
        });
    }

    function deleteexperiment_stuFun(id) {
        var row ;
        if (id == undefined) {//点击右键菜单才会触发这个
            var rows = experiment_stuDataGrid.datagrid('getSelections');
            id = rows[0].expstuno;
        } else {//点击操作里面的删除图标会触发这个
            experiment_stuDataGrid.datagrid('unselectAll').datagrid('uncheckAll');
        }
        parent.$.messager.confirm('询问', '您是否要退选当前实验?', function(b) {
            if (b) {
                progressLoad();
                $.post('${path }/dbexperiment_stu/delete', {
                    id : id
                }, function(result) {
                    if (result.success) {
                        parent.$.messager.alert('提示', result.msg, 'info');
                        experiment_stuDataGrid.datagrid('reload');
                    }
                    progressClose();
                }, 'JSON');
            }
        });
    }

</script>
<div class="easyui-layout" data-options="fit:true,border:false">
    <div data-options="region:'center',fit:true,border:false">
        <table id="experiment_stuDataGrid" data-options="fit:true,border:false"></table>
    </div>
</div>
<div id="experiment_stuToolbar" style="display: none;">
    <shiro:hasPermission name="/dbexperiment_stu/add">
        <a onclick="addexperiment_stuFun();" href="javascript:void(0);" class="easyui-linkbutton" data-options="plain:true,iconCls:'fi-plus icon-green'">选择实验</a>
    </shiro:hasPermission>
</div>