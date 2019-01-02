<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/commons/global.jsp" %>
<script type="text/javascript">
    var experimentFiles_experimentFilesDataGrid;
    var experimentFiles_experimentTree;

    $(function() {
        experimentFiles_experimentTree = $('#experimentFiles_experimentTree').tree({
            url : '${path }/dbexp/tree',
            parentField : 'pid',
            lines : true,
            onSelect : function(node) {
                experimentFiles_experimentFilesDataGrid.datagrid({
                    url : '${path }/dbexpfiles/dataGrid',
                    queryParams:{
                            expno: node.id
                        }
                });

            },
            onLoadSuccess:function(node,data){
               $("#experimentFiles_experimentTree li:eq(0)").find("div").addClass("tree-node-selected");   //设置第一个节点高亮
               var n = $("#experimentFiles_experimentTree").tree("getSelected");
               if(n!=null){
                    $("#experimentFiles_experimentTree").tree("select",n.target);    //相当于默认点击了一下第一个节点，执行onSelect方法
               }
            }
        });

        experimentFiles_experimentFilesDataGrid = $('#experimentFiles_experimentFilesDataGrid').datagrid({
            /*在加载数据时指定url*/
            //url : '${path }/dbexpfiles/dataGrid',
            fit : true,
            striped : true,
            rownumbers : true,
            pagination : true,
            singleSelect : true,
            idField : 'fileno',
            sortName : 'srcfilename',
	        sortOrder : 'asc',
            pageSize : 20,
            pageList : [ 10, 20, 30, 40, 50, 100, 200, 300, 400, 500 ],
            columns : [ [ {
                width : '250',
                title : '源文件',
                field : 'srcfilename',
                sortable : true
            }, {
                width : '480',
                title : '目标文件',
                field : 'dstfilename',
                sortable : true
            } , {
                field : 'action',
                title : '操作',
                width : 200,
                formatter : function(value, row, index) {
                    var str = '';
                        <shiro:hasPermission name="/dbexpfiles/edit">
                            str += $.formatString('<a href="javascript:void(0)" class="experimentFiles-easyui-linkbutton-edit" data-options="plain:true,iconCls:\'fi-pencil icon-blue\'" onclick="editexperimentFilesFun(\'{0}\');" >编辑</a>', row.fileno);
                        </shiro:hasPermission>

                        <shiro:hasPermission name="/dbexpfiles/delete">
                            str += '&nbsp;&nbsp;|&nbsp;&nbsp;';
                            str += $.formatString('<a href="javascript:void(0)" class="experimentFiles-easyui-linkbutton-del" data-options="plain:true,iconCls:\'fi-x icon-red\'" onclick="deleteexperimentFilesFun(\'{0}\');" >删除</a>', row.fileno);
                        </shiro:hasPermission>
                    return str;
                }
            }] ],
            onLoadSuccess:function(data){
                $('.experimentFiles-easyui-linkbutton-edit').linkbutton({text:'编辑'});
                $('.experimentFiles-easyui-linkbutton-config').linkbutton({text:'设置'});
                var btn = $('.experimentFiles-easyui-linkbutton-del').linkbutton({text:'删除'});
            },
            toolbar : '#experimentFilesToolbar'
        });

    });

    function cellStyler(value,row,index){
    			if (value == '未设置'){
    				return 'color:red;';
    			}
    }

    function addexperimentFilesFun() {
        var sel = $('#experimentFiles_experimentTree').tree('getSelected');

        parent.$.modalDialog({
            title : '添加',
            width : 700,
            height : 200,
            href : '${path }/dbexpfiles/addPage?id=' + sel.id,
            buttons : [ {
                text : '添加',
                handler : function() {
                    parent.$.modalDialog.openner_dataGrid = experimentFiles_experimentFilesDataGrid;//因为添加成功之后，需要刷新这个dataGrid，所以先预定义好
                    var f = parent.$.modalDialog.handler.find('#experimentFilesAddForm');
                    f.submit();
                }
            } ]
        });
    }

    function deleteexperimentFilesFun(id) {

        if (id == undefined) {//点击右键菜单才会触发这个
            var rows = experimentFiles_experimentFilesDataGrid.datagrid('getSelections');
            id = rows[0].fileno;
        } else {//点击操作里面的删除图标会触发这个
            experimentFiles_experimentFilesDataGrid.datagrid('unselectAll').datagrid('uncheckAll');
        }
        parent.$.messager.confirm('询问', '您是否要删除当前文件设置？', function(b) {
            if (b) {
                progressLoad();
                $.post('${path }/dbexpfiles/delete', {
                    id : id
                }, function(result) {
                    if (result.success) {
                        parent.$.messager.alert('提示', result.msg, 'info');
                        experimentFiles_experimentFilesDataGrid.datagrid('reload');
                    } else {
                        parent.$.messager.alert('错误', result.msg, 'error');
                    }
                    progressClose();
                }, 'JSON');
            }
        });
    }
    
    function editexperimentFilesFun(id) {
        if (id == undefined) {
            var rows = experimentFiles_experimentFilesDataGrid.datagrid('getSelections');
            id = rows[0].fileno;
        } else {
            experimentFiles_experimentFilesDataGrid.datagrid('unselectAll').datagrid('uncheckAll');
        }
        parent.$.modalDialog({
            title : '编辑',
            width : 700,
            height : 200,
            href : '${path }/dbexpfiles/editPage?id=' + id,
            buttons : [ {
                text : '确定',
                handler : function() {
                    parent.$.modalDialog.openner_dataGrid = experimentFiles_experimentFilesDataGrid;//因为添加成功之后，需要刷新这个dataGrid，所以先预定义好
                    var f = parent.$.modalDialog.handler.find('#experimentFilesEditForm');
                    f.submit();
                }
            } ]
        });
    }

</script>
<div class="easyui-layout" data-options="fit:true,border:false">

    <div data-options="region:'center',border:true,title:'文件列表'" >
        <table id="experimentFiles_experimentFilesDataGrid" data-options="fit:true,border:false"></table>
    </div>
    <div data-options="region:'west',border:true,split:false,title:'课程设计实验列表'"  style="width:300px;overflow: hidden; ">
        <ul id="experimentFiles_experimentTree" style="width:160px;margin: 10px 10px 10px 10px"></ul>
    </div>
</div>

<div id="experimentFilesToolbar" style="display: none;">
    <shiro:hasPermission name="/dbexpfiles/add">
        <a onclick="addexperimentFilesFun();" href="javascript:void(0);" class="easyui-linkbutton" data-options="plain:true,iconCls:'fi-plus icon-green'">添加</a>
    </shiro:hasPermission>

</div>