<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/commons/global.jsp" %>
<script type="text/javascript">

    const experimentStuExperiment = function () {
        let current_experiment_no = -1;
        let current_organization_id = -1;

        const tree = initTree();
        const datagrid = initDataGrid();
        const organization_id_select = getOrganizationIdSelect();

        /* region tree init */
        function initTree() {
            return $('#experiment-stu-experiment-tree').tree({
                url: '${path}/dbexp/tree',
                parentField: 'pid',
                lines: true,
                onSelect: treeOnSelect,
                onLoadSuccess: treeOnLoadSuccess
            });
        }

        function treeOnSelect(node) {
            current_experiment_no = node.id;
            refreshDataGrid();
        }

        function treeOnLoadSuccess(node, data) {
            tree.find("div").addClass("tree-node-selected");    // 高亮第一个节点
            const n = tree.tree("getSelected");
            if (n != null) tree.tree("select", n.target);
        }

        /* endregion */

        /* region datagrid init */
        function initDataGrid() {
            return $('#experiment_stu_experiment_stuDataGrid').datagrid({
                /*加载数据时指定 */
                //url : '${path}/dbexperiment_stu/experimentStuByExpno',
                striped: true,
                rownumbers: true,
                pagination: true,
                singleSelect: false,
                idField: 'expstuno',
                sortName: 'login_name',
                sortOrder: 'asc',
                remoteSort: false,
                pageSize: 20,
                pageList: [10, 20, 30, 40, 50, 100, 200, 300, 400, 500],
                frozenColumns: [[{
                    field: 'expstuno',
                    checkbox: true,
                }, {
                    width: '100',
                    title: '学号',
                    field: 'login_name',
                    sortable: true
                }, {
                    width: '100',
                    title: '姓名',
                    field: 'name',
                    sortable: true
                }, {
                    width: '100',
                    title: '提交时间',
                    field: 'submittime',
                    sortable: true
                }, {
                    width: '100',
                    title: '测试描述',
                    field: 'testdesc',
                    sortable: true,
                    formatter: testdescRowFormatter
                }, {
                    width: '200',
                    title: '相似度描述',
                    field: 'simdesc',
                    sortable: true,
                    sorter: simdescRowSorter,
                    formatter: simdescRowFormatter
                }, {
                    field: 'teststatus',
                    title: '操作',
                    width: 200,
                    formatter: teststatusRowFormatter
                }]],
                toolbar: '#experiment_stuToolbar'
            });
        }

        function testdescRowFormatter(value, row, index) {
            if (row.teststatus < 2) return value;
            return $.formatString("<a href='javascript:void(0)' onclick='experimentStuExperiment.openTestLog({0})'>{1}</a>", row.expstuno, value);
        }

        /**
         * 根据相似度排序的方法
         */
        function simdescRowSorter(a, b) {
            // 找到字符串中第一个数字作为排序依据
            // 格式：与(\d+)个同学的作业相似度超过xx%.
            const aa = a.match(/\d+/);
            const bb = b.match(/\d+/);
            if (!aa) return -1;
            if (!bb) return 1;
            const an = parseInt(aa.shift());
            const bn = parseInt(bb.shift());
            if (isNaN(an)) return -1;
            if (isNaN(bn)) return 1;

            return an > bn ? 1 : -1;
        }

        function simdescRowFormatter(value, row, index) {
            if (row.simstatus === 0) {
                return "正常";
            } else {
                str = $.formatString(
                    "<a href='javascript:void(0)' onclick='experimentStuExperiment.openSimCheckLog({0})' >{1}</a>",
                    row.expstuno,
                    value);
                return str;
            }
        }

        function teststatusRowFormatter(value, row, index) {
            let str = '';
            if (value === 0) {
                str = "正在测试..."
            } else if (value === -1) {
                str = "未测试";
            } else {
                str = "测试完成";
            }
            return str;
        }

        /* endregion */

        /* region select init */
        function getOrganizationIdSelect() {
            return $("#organizationid").combobox({
                onSelect: function (record) {
                    current_organization_id = record.value;
                    refreshDataGrid()
                }
            });
        }

        /* endregion */

        function refreshDataGrid() {
            datagrid.datagrid({
                url: '${path}/dbexperiment_stu/experimentStuByExpno',
                queryParams: {expno: current_experiment_no, organization_id: current_organization_id}
            });
        }

        function testRecord() {
            const rows = datagrid.datagrid('getSelections');

            const expstunos = [];
            if (rows && rows.length > 0) {
                for (let i = 0; i < rows.length; i++) {
                    expstunos.push(rows[i].expstuno);
                }
            }

            const form = new FormData();
            form.append("expstunos", expstunos);

            const xhr = new XMLHttpRequest();
            xhr.open("post", "${path}/dbexperiment_stu/checkBatch", false);
            xhr.send(form);
        }

        function openTestLog(expstuno) {
            parent.$.modalDialog({
                title: '查看测试日志',
                width: 900,
                height: 700,
                href: '${path}/dbexperiment_stu/openTestLogPage?expstuno=' + expstuno,
                buttons: [{
                    text: '关闭',
                    handler: function () {
                        parent.$.modalDialog.handler.dialog('close');
                    }
                }]
            });
        }

        function openSimCheckLog(expstuno) {
            parent.$.modalDialog({
                title: '相似的提交',
                width: 900,
                height: 700,
                href: '${path }/dbexperiment_stu/openSimCheckResultPage?expstuno=' + expstuno,
                buttons: [{
                    text: '关闭',
                    handler: function () {
                        parent.$.modalDialog.handler.dialog('close');
                    }
                }]
            });
        }

        return {
            datagrid: datagrid,
            tree: tree,
            organization_id_select: organization_id_select,
            testRecord: testRecord,
            openTestLog: openTestLog,
            openSimCheckLog: openSimCheckLog
        }
    }();

    window.experimentStuExperiment = experimentStuExperiment;
</script>

<div class="easyui-layout" data-options="fit:true,border:false">
    <div data-options="region:'west',border:true,split:false,title:'课程设计实验列表'" style="width:300px;overflow: hidden; ">
        <ul id="experiment-stu-experiment-tree" style="width:160px;margin: 10px 10px 10px 10px"></ul>
    </div>
    <div data-options="region:'center',border:true,title:'选此实验的学生'">
        <table id="experiment_stu_experiment_stuDataGrid" data-options="fit:true,border:false"></table>
    </div>

</div>

<div id="experiment_stuToolbar" style="display: none;">
    <label for="organizationid">选择班级</label>
    <select id="organizationid" name="organizationid" class="easyui-combobox"
            data-options="width:200,height:29,editable:false,panelHeight:'auto'">
        <option value='-1'>全部</option>
        <c:forEach items="${organizations}" var="organization">
            <option value="${organization.id}">${organization.name}</option>
        </c:forEach>
    </select>

    <shiro:hasPermission name="/dbexpfiles/add">
        <a onclick="experimentStuExperiment.testRecord()" href="javascript:void(0)" class="easyui-linkbutton"
           data-options="plain:true,iconCls:'fi-plus icon-green'">测试选中作业</a>
    </shiro:hasPermission>

</div>