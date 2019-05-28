package com.indeed.operators.rabbitmq.api;

import com.indeed.rabbitmq.admin.RabbitManagementApi;
import com.indeed.rabbitmq.admin.pojo.Bind;
import com.indeed.rabbitmq.admin.pojo.Binding;
import com.indeed.rabbitmq.admin.pojo.Channel;
import com.indeed.rabbitmq.admin.pojo.ClusterName;
import com.indeed.rabbitmq.admin.pojo.Connection;
import com.indeed.rabbitmq.admin.pojo.Consumer;
import com.indeed.rabbitmq.admin.pojo.Exchange;
import com.indeed.rabbitmq.admin.pojo.Extension;
import com.indeed.rabbitmq.admin.pojo.Node;
import com.indeed.rabbitmq.admin.pojo.OperatorPolicy;
import com.indeed.rabbitmq.admin.pojo.Overview;
import com.indeed.rabbitmq.admin.pojo.Parameter;
import com.indeed.rabbitmq.admin.pojo.Permission;
import com.indeed.rabbitmq.admin.pojo.Policy;
import com.indeed.rabbitmq.admin.pojo.Queue;
import com.indeed.rabbitmq.admin.pojo.Shovel;
import com.indeed.rabbitmq.admin.pojo.Status;
import com.indeed.rabbitmq.admin.pojo.User;
import com.indeed.rabbitmq.admin.pojo.VirtualHost;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

public class RabbitManagementApiFacade {

    private final RabbitManagementApi api;

    public RabbitManagementApiFacade(
            final RabbitManagementApi api
    ) {
        this.api = api;
    }

    public Overview getOverview() {
        return executeCall(() -> api.getOverview());
    }

    public Status getAliveness(final String vhost) {
        return executeCall(() -> api.getAliveness(vhost));
    }

    public ClusterName getClusterName() {
        return executeCall(() -> api.getClusterName());
    }

    public ResponseBody setClusterName(final ClusterName name) {
        return executeCall(() -> api.setClusterName(name));
    }

    public List<Binding> listBindings() {
        return executeCall(() -> api.listBindings());
    }

    public List<Binding> listBindings(final String vhost) {
        return executeCall(() -> api.listBindings(vhost));
    }

    public List<Binding> listBindingsFromExchange(final String vhost, final String exchange) {
        return executeCall(() -> api.listBindingsFromExchange(vhost, exchange));
    }

    public List<Binding> listBindingsToExchange(final String vhost, final String exchange) {
        return executeCall(() -> api.listBindingsToExchange(vhost, exchange));
    }

    public List<Binding> listBindingsToQueue(final String vhost, final String queue) {
        return executeCall(() -> api.listBindingsToQueue(vhost, queue));
    }

    public List<Binding> listExchangeToQueueBindings(final String vhost, final String exchange, final String queue) {
        return executeCall(() -> api.listExchangeToQueueBindings(vhost, exchange, queue));
    }

    public ResponseBody bindExchangeToQueue(final String vhost, final String exchange, final String queue, final Bind bind) {
        return executeCall(() -> api.bindExchangeToQueue(vhost, exchange, queue, bind));
    }

    public Binding getExchangeToQueueBinding(final String vhost, final String exchange, final String queue, final String bindingKey) {
        return executeCall(() -> api.getExchangeToQueueBinding(vhost, exchange, queue, bindingKey));
    }

    public ResponseBody deleteExchangeToQueueBinding(final String vhost, final String exchange, final String queue, final String bindingKey) {
        return executeCall(() -> api.deleteExchangeToQueueBinding(vhost, exchange, queue, bindingKey));
    }

    public List<Binding> listExchangeToExchangeBindings(final String vhost, final String source, final String destination) {
        return executeCall(() -> api.listExchangeToExchangeBindings(vhost, source, destination));
    }

    public ResponseBody bindExchangeToExchange(final String vhost, final String source, final String destination, final Bind bind) {
        return executeCall(() -> api.bindExchangeToExchange(vhost, source, destination, bind));
    }

    public Binding getExchangeToExchangeBinding(final String vhost, final String source, final String destination, final String bindingKey) {
        return executeCall(() -> api.getExchangeToExchangeBinding(vhost, source, destination, bindingKey));
    }

    public ResponseBody deleteExchangeToExchangeBinding(final String vhost, final String source, final String destination, final String bindingKey) {
        return executeCall(() -> api.deleteExchangeToExchangeBinding(vhost, source, destination, bindingKey));
    }

    public List<Channel> listChannels() {
        return executeCall(() -> api.listChannels());
    }

    public List<Channel> listChannels(final String vhost) {
        return executeCall(() -> api.listChannels(vhost));
    }

    public List<Channel> listConnectionChannels(final String name) {
        return executeCall(() -> api.listConnectionChannels(name));
    }

    public Channel getChannel(final String name) {
        return executeCall(() -> api.getChannel(name));
    }

    public List<Connection> listConnections() {
        return executeCall(() -> api.listConnections());
    }

    public List<Connection> listConnections(final String vhost) {
        return executeCall(() -> api.listConnections(vhost));
    }

    public Connection getConnection(final String name) {
        return executeCall(() -> api.getConnection(name));
    }

    public ResponseBody deleteConnection(final String name) {
        return executeCall(() -> api.deleteConnection(name));
    }

    public List<Consumer> listConsumers() {
        return executeCall(() -> api.listConsumers());
    }

    public List<Consumer> listConsumers(final String vhost) {
        return executeCall(() -> api.listConsumers(vhost));
    }

    public List<Exchange> listExchanges() {
        return executeCall(() -> api.listExchanges());
    }

    public List<Exchange> listExchanges(final String vhost) {
        return executeCall(() -> api.listExchanges(vhost));
    }

    public Exchange getExchange(final String vhost, final String name) {
        return executeCall(() -> api.getExchange(vhost, name));
    }

    public ResponseBody createExchange(final String vhost, final String name, final Exchange exchange) {
        return executeCall(() -> api.createExchange(vhost, name, exchange));
    }

    public ResponseBody deleteExchange(final String vhost, final String name) {
        return executeCall(() -> api.deleteExchange(vhost, name));
    }

    public List<Extension> listExtensions() {
        return executeCall(() -> api.listExtensions());
    }

    public List<Node> listNodes() {
        return executeCall(() -> api.listNodes());
    }

    public Node getNode(final String name) {
        return executeCall(() -> api.getNode(name));
    }

    public List<Parameter> listParameters() {
        return executeCall(() -> api.listParameters());
    }

    public List<Parameter> listParameters(final String component) {
        return executeCall(() -> api.listParameters(component));
    }

    public List<Parameter> listParameters(final String vhost, final String component) {
        return executeCall(() -> api.listParameters(vhost, component));
    }

    public Parameter getParameter(final String vhost, final String component, final String name) {
        return executeCall(() -> api.getParameter(vhost, component, name));
    }

    public ResponseBody createParameter(final String vhost, final String component, final String name, final Parameter parameter) {
        return executeCall(() -> api.createParameter(vhost, component, name, parameter));
    }

    public ResponseBody deleteParameter(final String vhost, final String component, final String name) {
        return executeCall(() -> api.deleteParameter(vhost, component, name));
    }

    public List<Permission> listPermissions() {
        return executeCall(() -> api.listPermissions());
    }

    public List<Permission> listPermissions(final String vhost) {
        return executeCall(() -> api.listPermissions(vhost));
    }

    public List<Permission> listUserPermissions(final String user) {
        return executeCall(() -> api.listUserPermissions(user));
    }

    public Permission getPermission(final String vhost, final String user) {
        return executeCall(() -> api.getPermission(vhost, user));
    }

    public ResponseBody createPermission(final String vhost, final String user, final Permission permission) {
        return executeCall(() -> api.createPermission(vhost, user, permission));
    }

    public ResponseBody deletePermission(final String vhost, final String user) {
        return executeCall(() -> api.deletePermission(vhost, user));
    }

    public List<Policy> listPolicies() {
        return executeCall(() -> api.listPolicies());
    }

    public List<Policy> listPolicies(final String vhost) {
        return executeCall(() -> api.listPolicies(vhost));
    }

    public Policy getPolicy(final String vhost, final String name) {
        return executeCall(() -> api.getPolicy(vhost, name));
    }

    public ResponseBody createPolicy(final String vhost, final String name, final Policy policy) {
        return executeCall(() -> api.createPolicy(vhost, name, policy));
    }

    public ResponseBody deletePolicy(final String vhost, final String name) {
        return executeCall(() -> api.deletePolicy(vhost, name));
    }

    public List<OperatorPolicy> listOperatorPolicies() {
        return executeCall(() -> api.listOperatorPolicies());
    }

    public List<OperatorPolicy> listOperatorPolicies(final String vhost) {
        return executeCall(() -> api.listOperatorPolicies(vhost));
    }

    public OperatorPolicy getOperatorPolicy(final String vhost, final String name) {
        return executeCall(() -> api.getOperatorPolicy(vhost, name));
    }

    public ResponseBody createOperatorPolicy(final String vhost, final String name, final OperatorPolicy policy) {
        return executeCall(() -> api.createOperatorPolicy(vhost, name, policy));
    }

    public ResponseBody deleteOperatorPolicy(final String vhost, final String name) {
        return executeCall(() -> api.deleteOperatorPolicy(vhost, name));
    }

    public List<Queue> listQueues() {
        return executeCall(() -> api.listQueues());
    }

    public List<Queue> listQueues(final String vhost) {
        return executeCall(() -> api.listQueues(vhost));
    }

    public Queue getQueue(final String vhost, final String name) {
        return executeCall(() -> api.getQueue(vhost, name));
    }

    public ResponseBody createQueue(final String vhost, final String name, final Queue queue) {
        return executeCall(() -> api.createQueue(vhost, name, queue));
    }

    public ResponseBody deleteQueue(final String vhost, final String name) {
        return executeCall(() -> api.deleteQueue(vhost, name));
    }

    public ResponseBody purgeQueue(final String vhost, final String name) {
        return executeCall(() -> api.purgeQueue(vhost, name));
    }

    public List<User> listUsers() {
        return executeCall(() -> api.listUsers());
    }

    public User getUser(final String name) {
        return executeCall(() -> api.getUser(name));
    }

    public ResponseBody createUser(final String name, final User user) {
        return executeCall(() -> api.createUser(name, user));
    }

    public ResponseBody deleteUser(final String name) {
        return executeCall(() -> api.deleteUser(name));
    }

    public User whoami() {
        return executeCall(() -> api.whoami());
    }

    public List<VirtualHost> listVirtualHosts() {
        return executeCall(() -> api.listVirtualHosts());
    }

    public VirtualHost getVirtualHost(final String vhost) {
        return executeCall(() -> api.getVirtualHost(vhost));
    }

    public ResponseBody createVirtualHost(final String vhost) {
        return executeCall(() -> api.createVirtualHost(vhost));
    }

    public ResponseBody deleteVirtualHost(final String vhost) {
        return executeCall(() -> api.deleteVirtualHost(vhost));
    }

    public List<Shovel> listShovels() {
        return executeCall(() -> api.listShovels());
    }

    public List<Shovel> listShovels(final String vhost) {
        return executeCall(() -> api.listShovels(vhost));
    }

    public Shovel getShovel(final String vhost, final String name) {
        return executeCall(() -> api.getShovel(vhost, name));
    }

    public ResponseBody createShovel(final String vhost, final String name, final Shovel shovel) {
        return executeCall(() -> api.createShovel(vhost, name, shovel));
    }

    public ResponseBody deleteShovel(final String vhost, final String name) {
        return executeCall(() -> api.deleteShovel(vhost, name));
    }

    private static <T> T executeCall(final Supplier<Call<T>> f) {
        final Call<T> call = f.get();
        try {
            final Response<T> response = call.execute();
            if (!response.isSuccessful()) {
                final String errorMessage;
                try {
                    errorMessage = response.errorBody().string();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }

                throw new RabbitManagementApiException(errorMessage);
            }

            return response.body();
        }
        catch (final IOException e) {
            throw new RabbitManagementApiException("failed", e);
        }
    }
}
