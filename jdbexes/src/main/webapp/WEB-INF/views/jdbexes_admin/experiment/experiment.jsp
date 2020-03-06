<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/commons/global.jsp" %>

<script type="text/javascript">
    const experiment = function () {
        const datagrid = initDataGrid();

        function initDataGrid() {
            return $('#experimentDataGrid').datagrid({
                url: '${path}/dbexp/dataGrid',
                striped: true,
                rownumbers: true,
                pagination: true,
                singleSelect: true,
                idField: 'expno',
                sortName: 'expno',
                sortOrder: 'asc',
                pageSize: 20,
                pageList: [10, 20, 30, 40, 50, 100, 200, 300, 400, 500],
                frozenColumns: [[{
                    width: '100',
                    title: '编号',
                    field: 'expno',
                    sortable: true
                }, {
                    width: '400',
                    title: '名称',
                    field: 'expname',
                    sortable: true
                }, {
                    width: '400',
                    title: '编译目标',
                    field: 'testtarget',
                    sortable: true
                }, {
                    width: '60',
                    title: '状态',
                    field: 'is_open',
                    sortable: true,
                    formatter: isOpenColFormatter
                }, {
                    field: 'action',
                    title: '操作',
                    width: 200,
                    formatter: actionColFormatter
                }]],
                onLoadSuccess: function (data) {
                    $('.experiment-easyui-linkbutton-edit').linkbutton({text: '编辑'});
                    $('.experiment-easyui-linkbutton-del').linkbutton({text: '删除'});
                },
                toolbar: '#experiment-toolbar'
            });
        }

        function isOpenColFormatter(value, row, index) {
            switch (value) {
                case 1:
                    return '正常';
                case 0:
                    return '停用';
            }
        }

        function actionColFormatter(value, row, index) {
            let str = '';
            <shiro:hasPermission name="/dbexp/edit">
            str += '&nbsp;&nbsp;|&nbsp;&nbsp;';
            str += $.formatString(`<a href="javascript:void(0)" class="experiment-easyui-linkbutton-edit"` +
                ` data-options="plain:true,iconCls:'fi-pencil icon-blue'"` +
                ` onclick="experiment.editRecord({0});" >编辑</a>`, row.expno);
            </shiro:hasPermission>
            <shiro:hasPermission name="/dbexp/delete">
            str += '&nbsp;&nbsp;|&nbsp;&nbsp;';
            str += $.formatString(`<a href="javascript:void(0)" class="experiment-easyui-linkbutton-del"` +
                ` data-options="plain:true,iconCls:'fi-x icon-red'"` +
                ` onclick="experiment.deleteRecord({0});" >删除</a>`, row.expno);
            </shiro:hasPermission>
            return str;
        }

        /* region 添加、修改、删除条目 */
        function addRecord() {
            parent.$.modalDialog({
                title: '添加',
                width: 500,
                height: 300,
                href: '${path}/dbexp/addPage',
                buttons: [{
                    text: '确定',
                    handler: function () {
                        parent.$.modalDialog.openner_dataGrid = datagrid; // 因为添加成功之后，需要刷新这个treeGrid，所以先预定义好
                        const f = parent.$.modalDialog.handler.find('#experiment-add-form');
                        f.submit();
                    }
                }]
            });
        }

        function editRecord(id) {
            if (id === undefined) {
                id = datagrid.datagrid('getSelections')[0].expno;
            } else {
                datagrid.datagrid('unselectAll').datagrid('uncheckAll');
            }
            parent.$.modalDialog({
                title: '编辑',
                width: 500,
                height: 300,
                href: '${path}/dbexp/editPage?id=' + id,
                buttons: [{
                    text: '确定',
                    handler: function () {
                        parent.$.modalDialog.openner_dataGrid = datagrid;
                        const f = parent.$.modalDialog.handler.find('#experiment-edit-form');
                        f.submit();
                    }
                }]
            });
        }

        function deleteRecord(id) {
            if (id === undefined) {//点击右键菜单才会触发这个
                const rows = datagrid.datagrid('getSelections');
                id = rows[0].expno;
            } else {//点击操作里面的删除图标会触发这个
                datagrid.datagrid('unselectAll').datagrid('uncheckAll');
            }
            parent.$.messager.confirm('询问', '您是否要删除当前实验?', function (b) {
                if (b) {
                    progressLoad();
                    $.post('${path}/dbexp/delete', {
                        id: id
                    }, function (result) {
                        if (result.success) {
                            parent.$.messager.alert('提示', result.msg, 'info');
                            datagrid.datagrid('reload');
                        }
                        progressClose();
                    }, 'JSON');
                }
            });
        }

        /* endregion */

        return {
            datagrid: datagrid,
            addRecord: addRecord,
            editRecord: editRecord,
            deleteRecord: deleteRecord
        };

    }();

    window.experiment = experiment
</script>

<div class="easyui-layout" data-options="fit:true,border:false">
    <div data-options="region:'center',fit:true,border:false">
        <table id="experimentDataGrid" data-options="fit:true,border:false"></table>
    </div>
</div>

<div id="experiment-toolbar" style="display: none;">
    <shiro:hasPermission name="/dbexp/add">
        <a onclick="experiment.addRecord()" href="javascript:void(0)" class="easyui-linkbutton"
           data-options="plain:true,iconCls:'fi-plus icon-green'">添加</a>
    </shiro:hasPermission>
</div>