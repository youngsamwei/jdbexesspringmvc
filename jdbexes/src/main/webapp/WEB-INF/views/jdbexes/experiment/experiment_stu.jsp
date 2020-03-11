<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/commons/global.jsp" %>

<script type="text/javascript">
    const experimentStu = function () {

        let datagrid = initDataGrid();

        /**
         * 初始化数据表
         */
        function initDataGrid() {
            return $('#experiment-stu-datagrid').datagrid({
                url: '${ path }/dbexperiment_stu/dataGrid',
                striped: true,
                rownumbers: true,
                pagination: true,
                singleSelect: true,
                idField: 'expstuno',
                sortName: 'expno',
                sortOrder: 'asc',
                pageSize: 20,
                pageList: [10, 20, 30, 40, 50, 100, 200, 300, 400, 500],
                frozenColumns: [[{
                    width: '400',
                    title: '实验名称',
                    field: 'expname',
                    sortable: true
                }, {
                    width: '200',
                    title: '测试描述',
                    field: 'testdesc',
                    sortable: true,
                    formatter: testdescRowFormatter
                }, {
                    width: '300',
                    title: '与已经提交作业相比',
                    field: 'simdesc',
                    sortable: true
                }, {
                    field: 'teststatus',
                    title: '操作',
                    width: 100,
                    align: 'center',
                    resizable: false,
                    formatter: function (value, row, index) {
                        if (value === 0) return "正在测试...";

                        let str = '';
                        <shiro:hasPermission name="/dbexperiment_stu/edit">
                        str += $.formatString(
                            '<a href="javascript:void(0)" class="experiment_stu-easyui-linkbutton-edit" data-options="plain:true,iconCls:\'fi-pencil icon-blue\'" onclick="experimentStu.editRecord({0});" >' +
                            '提交文件' +
                            '</a>',
                            row.expstuno);
                        </shiro:hasPermission>
                        return str;
                    }
                }]],
                onLoadSuccess: function (data) {
                    $('.experiment_stu-easyui-linkbutton-edit').linkbutton({text: '提交文件'});
                    $('.experiment_stu-easyui-linkbutton-del').linkbutton({text: '不选'});
                },
                toolbar: '#experiment-stu-toolbar'
            });
        }

        function testdescRowFormatter(value, row, index) {
            if (row.teststatus <= 0 || row.teststatus >= 5) return value;
            return $.formatString("<a href='javascript:void(0)' onclick='experimentStu.openTestLog({0})'>{1}</a>", row.expstuno, value);
        }

        /**
         * 加载测试结果的日志
         * @param expstuno
         */
        function openTestLog(expstuno) {
            parent.$.modalDialog({
                title: '查看测试日志',
                width: 900,
                height: 700,
                href: '${ path }/dbexperiment_stu/openTestLogPage?expstuno=' + expstuno,
                buttons: [{
                    text: '关闭',
                    handler: function () {
                        parent.$.modalDialog.handler.dialog('close');
                    }
                }]
            });
        }

        /**
         * 添加记录
         */
        function addRecord() {
            parent.$.modalDialog({
                title: '选择实验',
                width: 700,
                height: 600,
                href: '${ path }/dbexperiment_stu/addPage',
                buttons: [{
                    text: '确定',
                    handler: function () {
                        parent.$.modalDialog.openner_dataGrid = datagrid; // 因为添加成功之后，需要刷新这个treeGrid，所以先预定义好
                        const f = parent.$.modalDialog.handler.find('#experiment-stu-add-form');
                        f.submit();
                    }
                }]
            });
        }

        /**
         * 编辑记录
         * @param id
         */
        function editRecord(id) {
            if (id === undefined) {
                const rows = datagrid.datagrid('getSelections');
                id = rows[0].expstuno;
            } else {
                datagrid.datagrid('unselectAll').datagrid('uncheckAll');
            }
            parent.$.modalDialog({
                title: '上传文件',
                width: 800,
                height: 500,
                href: '${path}/dbexperiment_stu/submitFilePage?expstuno=' + id,
                buttons: [{
                    text: '上传文件并开始测试',
                    handler: function () {
                        parent.$.modalDialog.openner_dataGrid = datagrid; // 因为添加成功之后，需要刷新这个dataGrid，所以先预定义好
                        const f = parent.$.modalDialog.handler.find('#experiment-stu-submit-form');
                        f.submit();
                    }
                }]
            });
        }

        /**
         * 删除记录
         * @param id
         */
        function deleteRecord(id) {
            if (id === undefined) { // 点击右键菜单才会触发这个
                const rows = datagrid.datagrid('getSelections');
                id = rows[0].expstuno;
            } else { // 点击操作里面的删除图标会触发这个
                datagrid.datagrid('unselectAll').datagrid('uncheckAll');
            }
            parent.$.messager.confirm('询问', '您是否要退选当前实验?', function (b) {
                if (b) {
                    progressLoad();
                    $.post('${ path }/dbexperiment_stu/delete', {
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

        return {
            datagrid: datagrid,
            openTestLog: openTestLog,
            addRecord: addRecord,
            editRecord: editRecord,
            deleteRecord: deleteRecord
        };
    }();

    window.experimentStu = experimentStu
</script>

<div class="easyui-layout" data-options="fit:true,border:false">
    <div data-options="region:'center',fit:true,border:false">
        <table id="experiment-stu-datagrid" data-options="fit:true,border:false"></table>
    </div>
</div>

<div id="experiment-stu-toolbar" style="display: none;">
    <shiro:hasPermission name="/dbexperiment_stu/add">
        <a class="easyui-linkbutton" href="javascript:void(0)" onclick="experimentStu.addRecord()"
           data-options="plain:true,iconCls:'fi-plus icon-green'">选择实验</a>
    </shiro:hasPermission>
</div>