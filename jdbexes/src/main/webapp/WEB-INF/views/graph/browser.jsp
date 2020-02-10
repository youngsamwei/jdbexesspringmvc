<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/commons/global.jsp" %>

<script type="text/javascript" src="${staticPath }/static/neo4j_browser/vis.js" charset="utf-8"></script>
<script type="text/javascript" src="${staticPath }/static/neo4j_browser/layer.js" charset="utf-8"></script>
<link rel="stylesheet" type="text/css" href="${staticPath }/static/style/css/vis-network.min.css"/>

<style type="text/css">
    .network {
        width: 99%;
        height: 96%;
        border: 1px solid lightgray;
        background-color: #fff;
    }
</style>

<%--显示节点--%>
<%--<input type="checkbox" name="checkbox" checked value="Person"/>Person--%>
<%--<input type="checkbox" name="checkbox" checked value="Movie"/>Movie--%>

<a onclick="findSimilaritiesBySimValue();" href="javascript:void(0);" class="easyui-linkbutton"
   data-options="plain:true,iconCls:'fi-page-search icon-green'">查询</a>

<label for="simValue">相似度阈值</label>
<input name="simValue" id="simValue" type="text" placeholder="请输入相似度" class="easyui-numberbox span2"
       style="width: 50px; height: 20px;" data-options="required:true" value="100">

<label for="expid">选择实验</label>
<select id="expid" name="expid" class="easyui-combobox"
        data-options="width:200, height:29, panelHeight:'auto', editable:false,
        valueField: 'experimentid',
        textField: 'name',
        width:200, height:29, panelHeight:'auto', editable:false,
        mode: 'remote',
        url: '/graph/getExperiments',
        loadFilter: function(data){
            var opts = $(this).combobox('options');
            var emptyRow = {};
            emptyRow[opts.valueField] = '全部';
            emptyRow[opts.textField] = '全部';
            data.unshift(emptyRow);
            return data;
        },
        onLoadSuccess: function(items){
            $(this).combobox('select', '全部');
        }">
</select>

<label for="organizationid">选择班级</label>
<select id="organizationid" name="organizationid" class="easyui-combobox"
        data-options="width:200, height:29, panelHeight:'auto', editable:false,
        valueField: 'id',
        textField: 'name',
        url: '/graph/getOrganizations',
        loadFilter: function(data){
            var opts = $(this).combobox('options');
            var emptyRow = {};
            emptyRow[opts.valueField] = '全部';
            emptyRow[opts.textField] = '全部';
            data.unshift(emptyRow);
            return data;
        },
        onLoadSuccess: function(items){
            $(this).combobox('select', '全部');
        }">
</select>

<label for="stuid">选择学生</label>
<select id="stuid" name="stuid" class="easyui-combobox"
        data-options="width:200, height:29, panelHeight:'300', editable:false,
            valueField: 'studentid',
            textField: 'myfield',
            mode: 'remote',
            url: '/graph/getStudents',
            loadFilter: function(data){
                if ($.isArray(data)){ data = {total:data.length,rows:data}; }
                $.map(data.rows, function(row){ row.myfield = row.sno+'_'+row.name; });
                var opts = $(this).combobox('options');
                var emptyRow = {};
                emptyRow[opts.valueField] = '全部';
                emptyRow[opts.textField] = '全部';
                data.rows.unshift(emptyRow);
                return data.rows;
            },
            onLoadSuccess: function(items){
            $(this).combobox('select', '全部');
            }">
</select>

<label for="organizationid">选择班级</label>
<select id="organizationid" id="organizationid" class="easyui-combobox"
        data-options="width:200,height:29,editable:false,panelHeight:'auto'">
    <option>全部</option>
    <c:forEach items="${organizations}" var="organization">
        <option value="${organization.id}">${organization.name}</option>
    </c:forEach>
</select>

<%-- 拓扑图容器 --%>
<div id="network_id" class="network easyui-layout" data-options="fit:true,border:false"></div>
<script>
    const nodes = new vis.DataSet([]);  // 创建节点对象
    const edges = new vis.DataSet([]);  // 创建连线对象
    const nodeExtendArr = [];           // 已扩展的节点
    const exps = [];                    // 所有的实验集合
    let network;                        // 拓扑图

    $(function () {
        progressLoad();

        network = getNetwork();

        // 先初始化一个节点
        // TODO: 需要增加 等待图标
        $.ajax({
            url: '/graph/getSimilaritiesBySimValueExperimentidStudentid',
            async: true,
            data: {
                simValue: 100,
                expid: 6,
                stuid: 1
            },
            success: function (ret) {
                if (ret) {
                    const result = $.parseJSON(ret);
                    // console.info(result);
                    nodes.clear();
                    edges.clear();
                    createSimilarityNetwork(result);
                    // createNetwork(result);
                } else {
                    layer.msg("查询失败");
                }
                progressClose();
            }
        });
    });

    function findSimilaritiesBySimValue() {
        progressLoad();

        // 按相似度、学生、实验进行筛选
        const simValue = $('#simValue').numberbox('getValue');
        const exp_filter = $('#expid').combobox('getValue');
        const stu_filter = $('#stuid').combobox('getValue');
        const data = {simValue: simValue};
        let url = '/graph/getSimilarities';
        if (exp_filter !== '全部' && stu_filter !== '全部') {
            url = '/graph/getSimilaritiesBySimValueExperimentidStudentid';
            data.stuid = stu_filter;
            data.expid = exp_filter;
        } else if (exp_filter !== '全部') {
            url = '/graph/getSimilaritiesBySimValueExperimentid';
            data.expid = exp_filter;
        } else if (stu_filter !== '全部') {
            url = '/graph/getSimilaritiesBySimValueStudentid';
            data.stuid = stu_filter;
        }

        // 查询拓扑图数据
        $.ajax({
            url: url, async: true, data: data,
            success: function (ret) {
                if (ret) {
                    const result = $.parseJSON(ret);
                    // console.info(result);

                    // 按班级进行筛选
                    const organization_id = $('#organizationid').combobox('getValue');
                    if (organization_id !== '全部') {
                        $.ajax({ // 查询当前班级的所有学生编号
                            url: '/graph/getStudentIdByOrganizationId',
                            async: true,
                            data: {organization_id: organization_id},
                            success: function (ret) {
                                const ids = $.parseJSON(ret);
                                // console.info(ids);
                                // 更新网络
                                nodes.clear();
                                edges.clear();
                                createSimilarityNetwork(filterWithStudentId(result, ids));
                            }
                        })
                    } else {
                        // 更新网络
                        nodes.clear();
                        edges.clear();
                        createSimilarityNetwork(result);
                    }
                } else {
                    layer.msg("查询失败");
                }
                progressClose();
            }
        });
    }

    function filterWithStudentId(content, ids) {
        const student_filter = new Set();
        for (let i = 0; i < ids.length; ++i) {
            student_filter.add(ids[i])
        }
        const result = [];
        for (let i = 0; i < content.length; ++i) {
            const c = content[i];
            if (student_filter.has(c.a1.students[0].studentid)
                && student_filter.has(c.a2.students[0].studentid)) {
                result.push(c);
            }
        }
        return result;
    }

    /**
     * 获得 network 字段初始值
     */
    function getNetwork() {
        const container = document.getElementById('network_id');
        const data = {nodes: nodes, edges: edges};
        const options = {
            // 设置节点形状
            nodes: {
                shape: 'dot', // 采用远点的形式
                size: 30,
                font: {
                    face: 'Microsoft YaHei'
                }
            },
            // 设置关系连线
            edges: {
                font: {
                    face: 'Microsoft YaHei'
                }
            },
            // 设置节点的相互作用
            interaction: {
                hover: true // 鼠标经过改变样式
                // zoomView:false // 设置禁止缩放
            },
            // 力导向图效果
            physics: {
                enabled: true,
                barnesHut: {
                    gravitationalConstant: -4000,
                    centralGravity: 0.3,
                    springLength: 120,
                    springConstant: 0.04,
                    damping: 0.09,
                    avoidOverlap: 0
                }
            }
        };
        const network = new vis.Network(container, data, options);
        network.moveTo({scale: 0.8}); // 初始缩放
        // 拖拽结束：固定节点
        network.on("dragEnd", function (params) {
            if (params.nodes && params.nodes.length > 0) {
                network.clustering.updateClusteredNode(params.nodes[0], {physics: false});
            }
        });
        // 双击：扩展
        network.on("doubleClick", function (params) {
            // 取出当前节点在Vis的节点ID
            const nodeId = params.nodes[0];
            if (nodeExtendArr.indexOf(nodeId) !== -1) {
                layer.msg("该节点已经扩展");
            } else {
                getData(nodeId);
            }
        });
        return network;
    }

    /**
     * 获取id扩展后的数据
     */
    function getData(id) {
        const tipMsg = layer.msg('数据加载中，请稍等...', {icon: 16, shade: [0.1, '#000'], time: 0, offset: '250px'});
        // 该节点已扩展
        nodeExtendArr.push(id);
        $.ajax({
            url: '/getPath',
            data: { id: id }, // 当前节点id
            success: function (ret) {
                layer.close(tipMsg);
                if (ret) {
                    expandNetwork({nodes: ret.nodeList, edges: ret.edgeList});
                } else {
                    layer.msg("查询失败");
                }
            },
        });
    }

    function experiment_exists(exps, exp) {
        for (let i = 0; i < exps.length; i++){
            if (exps[i].id === exp.id){
                return true;
            }
        }
        return false;
    }

    function node_exists(node) {
        return nodes.get(node.id);
    }

    function processAssignment(assignment) {
        if (assignment.students) {
            const student = assignment.students[0];
            if (!node_exists(student)) {
                nodes.add({
                    id: student.id,
                    type: 'student',
                    label: student.name
                });
            }
            edges.add({
                /*id: edge.edgeId,*/
                arrows: 'to',
                from: student.id,
                to: assignment.id,
                /*label: sim.simValue,*/
                font: {align: "middle"},
                length: 150
            });
        }
    }

    /**
     * 创建相似度关系网络
     * @param sims 查询结果
     */
    function createSimilarityNetwork(sims) {
        for (let i = 0; i < sims.length; i++){
            const sim = sims[i];
            const a1 = sim.a1;
            const a2 = sim.a2;
            if (!node_exists(a1)) {
                nodes.add({
                    id: a1.id,
                    type: 'assignment',
                    label: a1.submitDate,
                    color: {
                        background: '#FFD86E'
                    }
                });
                processAssignment(a1);
            }
            if (!node_exists(a2)) {
                nodes.add({
                    id: a2.id,
                    type: 'assignment',
                    label: a2.submitDate,
                    color: {
                        background: '#FFD86E'
                    }
                });
                processAssignment(a2);
            }
            const value = sim.simValue.toFixed(2) + "";
            const length = 150 - sim.simValue.toFixed(0);
            edges.add({
                // id: edge.edgeId,
                arrows: 'to',
                from: a1.id,
                to: a2.id,
                label: value,
                font: {align: "middle"},
                length: length
            });
        }
        get_key_node();
    }

    /**
     * 查找只有入度没有出度的作业节点，并将其背景设为红色
     */
    function get_key_node() {
        const key_node = new Map();
        const non_key_node = new Map();
        edges.getIds().forEach(id => {
            const edge = edges.get(id);
            const node = nodes.get(edge.from);
            if (node.type === 'assignment') {
                non_key_node.set(edge.from, edge.from);
                key_node.set(edge.to, edge.to);
            }
        });
        non_key_node.forEach((val, key) => key_node.delete(key));
        key_node.forEach((val, key) => {
            const node = nodes.get(key);
            node.color.background = '#FF0000';
            nodes.update(node);
        });
    }

    /**
     * 扩展节点
     * @param param nodes和relation的集合
     */
    function expandNetwork(param) {
        for (let i = 0; i < param.length; i++){
            const node = param[i];
            nodes.add({
                id: node.id,
                label: node.name,
                color: {
                    background: '#FFD86E'
                }
            });
            if (node.assignments && node.assignments.length > 0) {
                var assignments = node.assignments;

                for (var j = 0; j < assignments.length; j++) {
                    nodes.add({
                        id: assignments[j].id,
                        label: assignments[j].submitDate,
                        color: {
                            background: '#6DCE9E'
                        }
                    });
                    edges.add({
                        /*id: edge.edgeId,*/
                        arrows: 'to',
                        from: node.id,
                        to: assignments[j].id,
                        /*label: label,*/
                        font: {align: "middle"},
                        length: 150
                    });
                    for (var k = 0; k < assignments[j].experiments.length; k++) {
                        if (!experiment_exists(exps, assignments[j].experiments[k])) {
                            exps.push(assignments[j].experiments[k]);
                            nodes.add({
                                id: assignments[j].experiments[k].id,
                                lable: assignments[j].experiments[k].name

                            });
                        }
                        edges.add({
                            arrows: 'to',
                            from: assignments[j].experiments[k].id,
                            to: assignments[j].id,
                            font: {align: "middle"},
                            length: 150
                        });

                    }
                }

            }
        }

    }

    $('input[type=checkbox][name=checkbox]').change(function (e) {
        for (const i in network.body.data.nodes._data) {
            if (network.body.data.nodes._data[i].label.indexOf("title") !== -1 && e.target.value === "Movie" && !e.currentTarget.checked) {
                network.clustering.updateClusteredNode(i, {hidden: true});
            } else if (network.body.data.nodes._data[i].label.indexOf("name") !== -1 && e.target.value === "Person" && !e.currentTarget.checked) {
                network.clustering.updateClusteredNode(i, {hidden: true});
            } else {
                network.clustering.updateClusteredNode(i, {hidden: false});
            }
        }
    });

    /**
     * 根据对象组数中的某个属性值进行过滤删除
     * @param arrName 数组名
     * @param field 过滤的字段
     * @param keyValue 字段值
     * @returns {null|*}
     */
    function deleteValueFromArr(arrName, field, keyValue) {
        if (arrName == null || arrName.length === 0) {
            return null;
        }
        for (let i = 0; i < arrName.length; i++) {
            if (arrName[i][field] === keyValue) {
                arrName.splice(i, 1);
            }
        }
        return arrName;
    }

    /**
     * 根据对象数组中的某个属性值获取过滤后的数组
     * @param arrName 数组名
     * @param field 过滤的字段
     * @param keyValue 字段值
     * @returns {[]}
     */
    function getArrFromArr(arrName, field, keyValue) {
        const arrReturn = [];
        if (arrName == null || arrName.length === 0) {
            return arrReturn;
        }
        let obj;
        for (let item = 0; item < arrName.length; item++) {
            obj = arrName[item];
            if (obj[field] === keyValue) {
                arrReturn.push(obj);
            }
        }
        return arrReturn;
    }

</script>