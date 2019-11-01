package com.nukkitx.network.raknet;

import com.nukkitx.network.NetworkUtils;
import com.nukkitx.network.util.DisconnectReason;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.EventLoop;

import javax.annotation.ParametersAreNonnullByDefault;
import java.net.InetSocketAddress;

@ParametersAreNonnullByDefault
public class RakNetClientSession extends RakNetSession {

    private final RakNetClient rakNet;
    private int connectionAttempts;
    private long nextConnectionAttempt;

    RakNetClientSession(RakNetClient rakNet, InetSocketAddress address, Channel channel, int mtu, EventLoop eventLoop) {
        super(address, channel, mtu, eventLoop);
        this.rakNet = rakNet;
        this.closed = true;
        this.setState(null);
    }

    @Override
    protected void onPacket(ByteBuf buffer) {
        int packetId = buffer.readUnsignedByte();

        switch (packetId) {
            case RakNetConstants.ID_OPEN_CONNECTION_REPLY_1:
                this.onOpenConnectionReply1(buffer);
                break;
            case RakNetConstants.ID_OPEN_CONNECTION_REPLY_2:
                this.onOpenConnectionReply2(buffer);
                break;
            case RakNetConstants.ID_CONNECTION_REQUEST_ACCEPTED:
                this.onConnectionRequestAccepted(buffer);
                break;
            case RakNetConstants.ID_CONNECTION_REQUEST_FAILED:
                this.close(DisconnectReason.CONNECTION_REQUEST_FAILED);
                break;
            case RakNetConstants.ID_INCOMPATIBLE_PROTOCOL_VERSION:
                this.close(DisconnectReason.INCOMPATIBLE_PROTOCOL_VERSION);
                break;
            case RakNetConstants.ID_ALREADY_CONNECTED:
                this.close(DisconnectReason.ALREADY_CONNECTED);
                break;
            case RakNetConstants.ID_NO_FREE_INCOMING_CONNECTIONS:
                this.close(DisconnectReason.NO_FREE_INCOMING_CONNECTIONS);
                break;
            case RakNetConstants.ID_IP_RECENTLY_CONNECTED:
                this.close(DisconnectReason.IP_RECENTLY_CONNECTED);
                break;
        }
    }

    @Override
    protected void tick(long curTime) {
        if (this.getState() == RakNetState.UNCONNECTED) {
            if (this.connectionAttempts >= RakNetConstants.MAXIMUM_CONNECTION_ATTEMPTS) {
                this.close(DisconnectReason.TIMED_OUT);
            } else {
                if (this.nextConnectionAttempt < curTime) {
                    this.attemptConnection(curTime);
                }
            }
        }
        
        super.tick(curTime);
    }

    private void attemptConnection(long curTime) {
        int mtuSize = RakNetConstants.MAXIMUM_MTU_SIZE - (this.connectionAttempts * (622));
        if (mtuSize < RakNetConstants.MINIMUM_MTU_SIZE) {
            mtuSize = RakNetConstants.MINIMUM_MTU_SIZE;
        }

        this.sendOpenConnectionRequest1(mtuSize);

        this.nextConnectionAttempt = curTime + 1000;
        this.connectionAttempts++;
    }

    @Override
    protected void onClose() {
        if (this.rakNet.session == this) {
            this.rakNet.session = null;
        }
    }

    @Override
    public RakNet getRakNet() {
        return this.rakNet;
    }

    private void onOpenConnectionReply1(ByteBuf buffer) {
        if (this.getState() != RakNetState.UNCONNECTED) {
            return;
        }
        if (!RakNetUtils.verifyUnconnectedMagic(buffer)) {
            return;
        }
        this.guid = buffer.readLong();
        boolean security = buffer.readBoolean();
        this.setMtu(buffer.readUnsignedShort());

        if (security) {
            this.close(DisconnectReason.CONNECTION_REQUEST_FAILED);
            return;
        }

        this.setState(RakNetState.INITIALIZING);

        this.sendOpenConnectionRequest2();
    }

    private void onOpenConnectionReply2(ByteBuf buffer) {
        if (this.getState() != RakNetState.INITIALIZING) {
            return;
        }
        if (!RakNetUtils.verifyUnconnectedMagic(buffer)) {
            this.close(DisconnectReason.CONNECTION_REQUEST_FAILED);
            return;
        }

        if (this.guid != buffer.readLong()) {
            this.close(DisconnectReason.CONNECTION_REQUEST_FAILED);
            return;
        }
        NetworkUtils.readAddress(buffer);
        this.setMtu(buffer.readUnsignedShort());
        buffer.readBoolean();

        this.initialize();
        this.setState(RakNetState.INITIALIZED);

        this.sendConnectionRequest();
    }

    private void onConnectionRequestAccepted(ByteBuf buffer) {
        NetworkUtils.readAddress(buffer);
        buffer.readUnsignedShort();
        while (buffer.readableBytes() > 16) {
            NetworkUtils.readAddress(buffer);
        }

        this.sendNewIncomingConnection(buffer.readLong());

        this.setState(RakNetState.CONNECTED);
    }

    private void sendOpenConnectionRequest1(int mtuSize) {
        ByteBuf buffer = this.allocateBuffer(mtuSize);
        buffer.writeByte(RakNetConstants.ID_OPEN_CONNECTION_REQUEST_1);
        RakNetUtils.writeUnconnectedMagic(buffer);
        buffer.writeByte(RakNetConstants.RAKNET_PROTOCOL_VERSION);
        buffer.writeZero(mtuSize - 46);

        this.sendDirect(buffer);
    }

    private void sendOpenConnectionRequest2() {
        ByteBuf buffer = this.allocateBuffer(34);
        buffer.writeByte(RakNetConstants.ID_OPEN_CONNECTION_REQUEST_2);
        RakNetUtils.writeUnconnectedMagic(buffer);
        NetworkUtils.writeAddress(buffer, this.address);
        buffer.writeShort(this.getMtu());
        buffer.writeLong(this.rakNet.guid);

        this.sendDirect(buffer);
    }

    private void sendConnectionRequest() {
        ByteBuf buffer = this.allocateBuffer(18);

        buffer.writeByte(RakNetConstants.ID_CONNECTION_REQUEST);
        buffer.writeLong(this.rakNet.guid);
        buffer.writeLong(System.currentTimeMillis());
        buffer.writeBoolean(false);

        this.send(buffer, RakNetPriority.IMMEDIATE, RakNetReliability.RELIABLE_ORDERED);
    }

    private void sendNewIncomingConnection(long pingTime) {
        ByteBuf buffer = this.allocateBuffer(this.isIpv6Session() ? 626 : 164);

        buffer.writeByte(RakNetConstants.ID_NEW_INCOMING_CONNECTION);
        NetworkUtils.writeAddress(buffer, address);
        for (InetSocketAddress address : this.isIpv6Session() ? RakNetUtils.LOCAL_IP_ADDRESSES_V6 : RakNetUtils.LOCAL_IP_ADDRESSES_V4) {
            NetworkUtils.writeAddress(buffer, address);
        }
        buffer.writeLong(pingTime);
        buffer.writeLong(System.currentTimeMillis());

        this.send(buffer, RakNetPriority.IMMEDIATE, RakNetReliability.RELIABLE_ORDERED);
    }
}
